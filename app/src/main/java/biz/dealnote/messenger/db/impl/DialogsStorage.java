package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.DialogsColumns;
import biz.dealnote.messenger.db.column.PeersColumns;
import biz.dealnote.messenger.db.interfaces.IDialogsStorage;
import biz.dealnote.messenger.db.model.PeerPatch;
import biz.dealnote.messenger.db.model.entity.DialogEntity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.db.model.entity.SimpleDialogEntity;
import biz.dealnote.messenger.model.Chat;
import biz.dealnote.messenger.model.ChatAction;
import biz.dealnote.messenger.model.criteria.DialogsCriteria;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by hp-dv6 on 04.06.2016.
 * VKMessenger
 */
class DialogsStorage extends AbsStorage implements IDialogsStorage {

    private PublishSubject<IDialogUpdate> updatePublishSubject;
    private PublishSubject<IDeletedDialog> dialogsDeletingPublisher;
    private PublishSubject<Pair<Integer, Integer>> unreadDialogsCounter;

    private SharedPreferences preferences;

    DialogsStorage(@NonNull AppStorages base) {
        super(base);
        this.updatePublishSubject = PublishSubject.create();
        this.dialogsDeletingPublisher = PublishSubject.create();
        this.preferences = base.getSharedPreferences("dialogs_prefs", Context.MODE_PRIVATE);
        this.unreadDialogsCounter = PublishSubject.create();
    }

    private static String unreadKeyFor(int accountId) {
        return "unread" + accountId;
    }

    @Override
    public int getUnreadDialogsCount(int accountId) {
        synchronized (this) {
            return preferences.getInt(unreadKeyFor(accountId), 0);
        }
    }

    @Override
    public Observable<Pair<Integer, Integer>> observeUnreadDialogsCount() {
        return unreadDialogsCounter;
    }

    @Override
    public Single<List<DialogEntity>> getDialogs(@NonNull DialogsCriteria criteria) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getDialogsContentUriFor(criteria.getAccountId());

            Cursor cursor = getContext().getContentResolver().query(uri, null, null,
                    null, DialogsColumns.LAST_MESSAGE_ID + " DESC");

            List<DialogEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    dbos.add(mapEntity(cursor));
                }

                cursor.close();
            }

            e.onSuccess(dbos);
            Exestime.log("getDialogs", start);
        });
    }

    @Override
    public Completable removePeerWithId(int accountId, int peerId) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            getContentResolver().delete(uri, DialogsColumns._ID + " = ?", new String[]{String.valueOf(peerId)});
            emitter.onComplete();

            dialogsDeletingPublisher.onNext(new DialogEventImpl(accountId, peerId));
        });
    }

    @Override
    public Completable updatePeerWithId(int accountId, int peerId, int lastMessageId, int unreadCount) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);

            ContentValues cv = new ContentValues();
            cv.put(DialogsColumns.LAST_MESSAGE_ID, lastMessageId);
            cv.put(DialogsColumns.UNREAD, unreadCount);

            String where = DialogsColumns._ID + " = ?";
            String[] args = {String.valueOf(peerId)};
            getContext().getContentResolver().update(uri, cv, where, args);
            emitter.onComplete();

            updatePublishSubject.onNext(new DialogUpdateImpl(accountId, peerId, lastMessageId, unreadCount));
        });
    }

    @Override
    public Completable insertDialogs(int accountId, List<DialogEntity> entities, boolean clearBefore) {
        return Completable.create(emitter -> {
            final long start = System.currentTimeMillis();
            final Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBefore) {
                operations.add(ContentProviderOperation.newDelete(uri).build());
            }

            for (DialogEntity entity : entities) {
                SimpleDialogEntity simple = entity.simplify();

                operations.add(ContentProviderOperation.newInsert(uri).withValues(createCv(entity)).build());

                operations.add(ContentProviderOperation
                        .newInsert(MessengerContentProvider.getPeersContentUriFor(accountId))
                        .withValues(createPeerCv(simple))
                        .build());

                MessagesStorage.appendDboOperation(accountId, entity.getMessage(), operations, null, null);
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();

            Exestime.log("DialogsStorage.insertDialogs", start, "count: " + entities.size() + ", clearBefore: " + clearBefore);
        });
    }

    @Override
    public Completable changeTitle(int accountId, int peedId, String title) {
        return Completable.fromAction(() -> {
            final Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            ContentValues cv = new ContentValues();
            cv.put(DialogsColumns.TITLE, title);
            getContentResolver().update(uri, cv, DialogsColumns._ID + " = ?", new String[]{String.valueOf(peedId)});
        });
    }

    private ContentValues createCv(DialogEntity entity) {
        ContentValues cv = new ContentValues();
        MessageEntity messageDbo = entity.getMessage();

        cv.put(DialogsColumns._ID, messageDbo.getPeerId());
        cv.put(DialogsColumns.UNREAD, entity.getUnreadCount());
        cv.put(DialogsColumns.TITLE, entity.getTitle());
        cv.put(DialogsColumns.IN_READ, entity.getInRead());
        cv.put(DialogsColumns.OUT_READ, entity.getOutRead());
        cv.put(DialogsColumns.PHOTO_50, entity.getPhoto50());
        cv.put(DialogsColumns.PHOTO_100, entity.getPhoto100());
        cv.put(DialogsColumns.PHOTO_200, entity.getPhoto200());
        cv.put(DialogsColumns.LAST_MESSAGE_ID, messageDbo.getId());
        return cv;
    }

    private ContentValues createPeerCv(SimpleDialogEntity entity) {
        ContentValues cv = new ContentValues();
        cv.put(PeersColumns._ID, entity.getPeerId());
        cv.put(PeersColumns.UNREAD, entity.getUnreadCount());
        cv.put(PeersColumns.TITLE, entity.getTitle());
        cv.put(PeersColumns.IN_READ, entity.getInRead());
        cv.put(PeersColumns.OUT_READ, entity.getOutRead());
        cv.put(PeersColumns.PHOTO_50, entity.getPhoto50());
        cv.put(PeersColumns.PHOTO_100, entity.getPhoto100());
        cv.put(PeersColumns.PHOTO_200, entity.getPhoto200());
        cv.put(PeersColumns.PINNED, serializeJson(entity.getPinned()));
        return cv;
    }

    @Override
    public Completable saveSimple(int accountId, @NonNull SimpleDialogEntity entity) {
        return Completable.create(emitter -> {
            final Uri uri = MessengerContentProvider.getPeersContentUriFor(accountId);
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newInsert(uri).withValues(createPeerCv(entity)).build());
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Optional<SimpleDialogEntity>> findSimple(int accountId, int peerId) {
        return Single.create(emitter -> {
            String[] projection = {
                    PeersColumns.UNREAD,
                    PeersColumns.TITLE,
                    PeersColumns.IN_READ,
                    PeersColumns.OUT_READ,
                    PeersColumns.PHOTO_50,
                    PeersColumns.PHOTO_100,
                    PeersColumns.PHOTO_200,
                    PeersColumns.PINNED
            };

            Uri uri = MessengerContentProvider.getPeersContentUriFor(accountId);
            Cursor cursor = getContentResolver().query(uri, projection,
                    PeersColumns.FULL_ID + " = ?", new String[]{String.valueOf(peerId)}, null);

            SimpleDialogEntity entity = null;
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    entity = new SimpleDialogEntity(peerId)
                            .setTitle(cursor.getString(cursor.getColumnIndex(PeersColumns.TITLE)))
                            .setPhoto200(cursor.getString(cursor.getColumnIndex(PeersColumns.PHOTO_200)))
                            .setPhoto100(cursor.getString(cursor.getColumnIndex(PeersColumns.PHOTO_100)))
                            .setPhoto50(cursor.getString(cursor.getColumnIndex(PeersColumns.PHOTO_50)))
                            .setInRead(cursor.getInt(cursor.getColumnIndex(PeersColumns.IN_READ)))
                            .setOutRead(cursor.getInt(cursor.getColumnIndex(PeersColumns.OUT_READ)))
                            .setPinned(deserializeJson(cursor, PeersColumns.PINNED, MessageEntity.class));
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(entity));
        });
    }

    @Override
    public void setUnreadDialogsCount(int accountId, int unreadCount) {
        synchronized (this) {
            preferences.edit()
                    .putInt(unreadKeyFor(accountId), unreadCount)
                    .apply();
        }

        unreadDialogsCounter.onNext(new Pair<>(accountId, unreadCount));
    }

    @Override
    public Single<Collection<Integer>> getMissingGroupChats(int accountId, @NonNull Collection<Integer> ids) {
        return Single.create(e -> {
            if (ids.isEmpty()) {
                e.onSuccess(Collections.emptyList());
                return;
            }

            Set<Integer> peerIds = new HashSet<>(ids);
            String[] projection = {DialogsColumns._ID};
            Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            Cursor cursor = getContentResolver().query(uri, projection,
                    DialogsColumns.FULL_ID + " IN (" + TextUtils.join(",", peerIds) + ")", null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int peerId = cursor.getInt(cursor.getColumnIndex(DialogsColumns._ID));
                    peerIds.remove(peerId);
                }

                cursor.close();
            }

            e.onSuccess(peerIds);
        });
    }

    @Override
    public Observable<IDialogUpdate> observeDialogUpdates() {
        return updatePublishSubject;
    }

    @Override
    public Completable insertChats(int accountId, List<VKApiChat> chats) {
        return Completable.fromAction(() -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(chats.size());

            for (VKApiChat chat : chats) {
                operations.add(ContentProviderOperation
                        .newInsert(MessengerContentProvider.getDialogsContentUriFor(accountId))
                        .withValues(DialogsColumns.getCV(chat))
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
        });
    }

    @Override
    public Observable<IDeletedDialog> observeDialogsDeleting() {
        return dialogsDeletingPublisher;
    }

    @Override
    public Completable applyPatches(int accountId, @NonNull List<PeerPatch> patches) {
        return Completable.create(emitter -> {
            Uri dialogsUri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            Uri peersUri = MessengerContentProvider.getPeersContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>(patches.size() * 2);

            for (PeerPatch patch : patches) {
                ContentValues dialogscv = new ContentValues();
                ContentValues peerscv = new ContentValues();

                if (nonNull(patch.getInRead())) {
                    dialogscv.put(DialogsColumns.IN_READ, patch.getInRead().getId());
                    dialogscv.put(DialogsColumns.UNREAD, patch.getInRead().getUnreadCount());
                    peerscv.put(PeersColumns.IN_READ, patch.getInRead().getId());
                    peerscv.put(PeersColumns.UNREAD, patch.getInRead().getUnreadCount());
                }

                if (nonNull(patch.getOutRead())) {
                    dialogscv.put(DialogsColumns.OUT_READ, patch.getOutRead().getId());
                    peerscv.put(PeersColumns.OUT_READ, patch.getOutRead().getId());
                }

                String[] args = {String.valueOf(patch.getId())};

                if (dialogscv.size() > 0) {
                    operations.add(ContentProviderOperation.newUpdate(dialogsUri)
                            .withSelection(DialogsColumns._ID + " = ?", args)
                            .withValues(dialogscv)
                            .build());
                }

                if (peerscv.size() > 0) {
                    operations.add(ContentProviderOperation.newUpdate(peersUri)
                            .withSelection(PeersColumns._ID + " = ?", args)
                            .withValues(peerscv)
                            .build());
                }
            }

            if (nonEmpty(operations)) {
                getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            }

            emitter.onComplete();
        });
    }

    @Override
    public Single<Optional<Chat>> findChatById(int accountId, int peerId) {
        return Single.fromCallable(() -> {
            String[] projection = {
                    DialogsColumns.TITLE,
                    DialogsColumns.PHOTO_200,
                    DialogsColumns.PHOTO_100,
                    DialogsColumns.PHOTO_50,
            };

            Uri uri = MessengerContentProvider.getDialogsContentUriFor(accountId);
            Cursor cursor = getContentResolver().query(uri, projection,
                    DialogsColumns.FULL_ID + " = ?", new String[]{String.valueOf(peerId)}, null);

            Chat chat = null;
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    chat = new Chat(peerId);
                    chat.setTitle(cursor.getString(cursor.getColumnIndex(DialogsColumns.TITLE)))
                            .setPhoto200(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_200)))
                            .setPhoto100(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_100)))
                            .setPhoto50(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_50)));
                }

                cursor.close();
            }

            return Optional.wrap(chat);
        });
    }

    private DialogEntity mapEntity(@NonNull Cursor cursor) {
        @ChatAction
        int action = cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_ACTION));

        boolean encrypted = cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_ENCRYPTED)) == 1;

        int messageId = cursor.getInt(cursor.getColumnIndex(DialogsColumns.LAST_MESSAGE_ID));
        int peerId = cursor.getInt(cursor.getColumnIndex(DialogsColumns._ID));
        int fromId = cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_FROM_ID));

        MessageEntity message = new MessageEntity(messageId, peerId, fromId)
                .setBody(cursor.getString(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_BODY)))
                .setDate(cursor.getLong(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_DATE)))
                .setOut(cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_OUT)) == 1)
                //.setTitle(cursor.getString(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_TITLE)))
                //.setRead(cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_READ_STATE)) == 1)
                .setHasAttachmens(cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_HAS_ATTACHMENTS)) == 1)
                .setForwardCount(cursor.getInt(cursor.getColumnIndex(DialogsColumns.FOREIGN_MESSAGE_FWD_COUNT)))
                .setAction(action)
                .setEncrypted(encrypted);

        return new DialogEntity(peerId)
                .setMessage(message)
                .setInRead(cursor.getInt(cursor.getColumnIndex(DialogsColumns.IN_READ)))
                .setOutRead(cursor.getInt(cursor.getColumnIndex(DialogsColumns.OUT_READ)))
                .setTitle(cursor.getString(cursor.getColumnIndex(DialogsColumns.TITLE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(DialogsColumns.PHOTO_200)))
                .setUnreadCount(cursor.getInt(cursor.getColumnIndex(DialogsColumns.UNREAD)))
                .setLastMessageId(cursor.getInt(cursor.getColumnIndex(DialogsColumns.LAST_MESSAGE_ID)));
    }

    private static class DialogUpdateImpl implements IDialogUpdate {

        final int accountId;
        final int peerId;
        final int lastMessageId;
        final int unreadCount;

        private DialogUpdateImpl(int accountId, int peerId, int lastMessageId, int unreadCount) {
            this.accountId = accountId;
            this.peerId = peerId;
            this.lastMessageId = lastMessageId;
            this.unreadCount = unreadCount;
        }

        @Override
        public int getAccountId() {
            return accountId;
        }

        @Override
        public int getPeerId() {
            return peerId;
        }

        @Override
        public int getLastMessageId() {
            return lastMessageId;
        }

        @Override
        public int getUnreadCount() {
            return unreadCount;
        }
    }

    private static class DialogEventImpl implements IDeletedDialog {

        final int accountId;
        final int peerId;

        private DialogEventImpl(int accountId, int peerId) {
            this.accountId = accountId;
            this.peerId = peerId;
        }

        @Override
        public int getAccountId() {
            return accountId;
        }

        @Override
        public int getPeerId() {
            return peerId;
        }
    }
}
package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.DBHelper;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.RecordNotFoundException;
import biz.dealnote.messenger.db.column.MessageColumns;
import biz.dealnote.messenger.db.interfaces.Cancelable;
import biz.dealnote.messenger.db.interfaces.IMessagesStore;
import biz.dealnote.messenger.db.model.MessagePatch;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.ChatAction;
import biz.dealnote.messenger.model.DraftMessage;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.model.MessageUpdate;
import biz.dealnote.messenger.model.criteria.MessagesCriteria;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.join;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by hp-dv6 on 01.06.2016.
 * VKMessenger
 */
class MessagesStore extends AbsStore implements IMessagesStore {

    private static final String ORDER_BY = MessageColumns.FULL_STATUS + ", " + MessageColumns.FULL_ID;

    MessagesStore(@NonNull AppStores base) {
        super(base);
    }

    static int appendDboOperation(int accountId, @NonNull MessageEntity dbo, @NonNull List<ContentProviderOperation> target, Integer attachToId, Integer attachToIndex) {
        ContentValues cv = new ContentValues();

        if (nonNull(attachToId)) {
            // если есть ID сообщения, к которому прикреплено dbo
            cv.put(MessageColumns.ATTACH_TO, attachToId);
        } else if (isNull(attachToIndex)) {
            // если сообщение не прикреплено к другому
            cv.put(MessageColumns._ID, dbo.getId());
            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
        }

        cv.put(MessageColumns.PEER_ID, dbo.getPeerId());
        cv.put(MessageColumns.FROM_ID, dbo.getFromId());
        cv.put(MessageColumns.DATE, dbo.getDate());
        cv.put(MessageColumns.READ_STATE, dbo.isRead());
        cv.put(MessageColumns.OUT, dbo.isOut());
        cv.put(MessageColumns.TITLE, dbo.getTitle());
        cv.put(MessageColumns.BODY, dbo.getBody());
        cv.put(MessageColumns.ENCRYPTED, dbo.isEncrypted());
        cv.put(MessageColumns.IMPORTANT, dbo.isImportant());
        cv.put(MessageColumns.DELETED, dbo.isDeleted());
        cv.put(MessageColumns.FORWARD_COUNT, dbo.getForwardCount());
        cv.put(MessageColumns.HAS_ATTACHMENTS, dbo.isHasAttachmens());
        cv.put(MessageColumns.STATUS, dbo.getStatus());
        cv.put(MessageColumns.ORIGINAL_ID, dbo.getOriginalId());
        cv.put(MessageColumns.CHAT_ACTIVE, dbo.getChatActive());
        cv.put(MessageColumns.USER_COUNT, dbo.getUsersCount());
        cv.put(MessageColumns.ADMIN_ID, dbo.getAdminId());
        cv.put(MessageColumns.ACTION, dbo.getAction());
        cv.put(MessageColumns.ACTION_MID, dbo.getActionMemberId());
        cv.put(MessageColumns.ACTION_EMAIL, dbo.getActionEmail());
        cv.put(MessageColumns.ACTION_TEXT, dbo.getActionText());
        cv.put(MessageColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(MessageColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(MessageColumns.PHOTO_200, dbo.getPhoto200());
        cv.put(MessageColumns.RANDOM_ID, dbo.getRandomId());
        cv.put(MessageColumns.EXTRAS, isNull(dbo.getExtras()) ? null : GSON.toJson(dbo.getExtras()));

        Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri)
                .withValues(cv);

        // если сообщение прикреплено к другому, но его ID на данный момент неизвестен
        if (isNull(attachToId) && nonNull(attachToIndex)) {
            builder.withValueBackReference(MessageColumns.ATTACH_TO, attachToIndex);
        }

        int index = addToListAndReturnIndex(target, builder.build());

        if (dbo.isHasAttachmens()) {
            List<Entity> entities = dbo.getAttachments();

            for (Entity attachmentEntity : entities) {
                AttachmentsStore.appendAttachOperationWithBackReference(target, accountId, AttachToType.MESSAGE, index, attachmentEntity);
            }
        }

        if (dbo.getForwardCount() > 0) {
            for (MessageEntity fwdDbo : dbo.getForwardMessages()) {
                appendDboOperation(accountId, fwdDbo, target, null, index);
            }
        }

        return index;
    }

    @Override
    public Completable insertPeerDbos(int accountId, int peerId, @NonNull List<MessageEntity> dbos, boolean clearHistory) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearHistory) {
                Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
                String where = MessageColumns.PEER_ID + " = ? AND " + MessageColumns.ATTACH_TO + " = ? AND " + MessageColumns.STATUS + " = ?";
                String[] args = new String[]{String.valueOf(peerId), String.valueOf(MessageColumns.DONT_ATTACH), String.valueOf(MessageStatus.SENT)};

                operations.add(ContentProviderOperation.newDelete(uri).withSelection(where, args).build());
            }

            for (MessageEntity dbo : dbos) {
                appendDboOperation(accountId, dbo, operations, null, null);
            }

            getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<int[]> insertDbos(int accountId, @NonNull List<MessageEntity> dbos) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            final int[] indexes = new int[dbos.size()];

            for (int i = 0; i < dbos.size(); i++) {
                MessageEntity dbo = dbos.get(i);
                int index = appendDboOperation(accountId, dbo, operations, null, null);

                indexes[i] = index;
            }

            ContentProviderResult[] results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            final int[] ids = new int[dbos.size()];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            emitter.onSuccess(ids);
        });
    }

    @Override
    public Single<Optional<Integer>> findLastSentMessageIdForPeer(int accountId, int peerId) {
        return Single.create(emitter -> {
            final Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            final String[] projection = {MessageColumns._ID};

            String where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.STATUS + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ?" +
                    " AND " + MessageColumns.DELETED + " = ?";

            String[] args = {String.valueOf(peerId),
                    String.valueOf(MessageStatus.SENT),
                    String.valueOf(MessageColumns.DONT_ATTACH),
                    "0"
            };

            Cursor cursor = getContentResolver().query(uri, projection, where, args, MessageColumns.FULL_ID + " DESC LIMIT 1");

            Integer id = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex(MessageColumns._ID));
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(id));
        });
    }

    @Override
    public Single<Integer> calculateUnreadCount(int accountId, int peerId) {
        return Single.fromCallable(() -> {
            int result = 0;
            Cursor cursor = DBHelper.getInstance(getContext(), accountId)
                    .getReadableDatabase()
                    .rawQuery("SELECT COUNT(" + MessageColumns._ID + ") FROM " + MessageColumns.TABLENAME +
                                    " WHERE " + MessageColumns.PEER_ID + " = ?" +
                                    " AND " + MessageColumns.READ_STATE + " = ?" +
                                    " AND " + MessageColumns.OUT + " = ?" +
                                    " AND " + MessageColumns.ATTACH_TO + " = ?" +
                                    " AND " + MessageColumns.DELETED + " = ?",
                            new String[]{String.valueOf(peerId), "0", "0", "0", "0"});

            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }

            cursor.close();
            return result;
        });
    }

    private Cursor queryMessagesByCriteria(MessagesCriteria criteria) {
        String where;
        String[] args;

        if (criteria.getStartMessageId() == null) {
            where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ?" +
                    " AND " + MessageColumns.STATUS + " != ?";

            args = new String[]{
                    String.valueOf(criteria.getPeerId()),
                    "0",
                    String.valueOf(MessageStatus.EDITING)
            };
        } else {
            where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ? " +
                    " AND " + MessageColumns.FULL_ID + " < ? " +
                    " AND " + MessageColumns.STATUS + " != ?";

            args = new String[]{
                    String.valueOf(criteria.getPeerId()),
                    "0",
                    String.valueOf(criteria.getStartMessageId()),
                    String.valueOf(MessageStatus.EDITING)
            };
        }

        Uri uri = MessengerContentProvider.getMessageContentUriFor(criteria.getAccountId());
        return getContext().getContentResolver().query(uri, null, where, args, ORDER_BY);
    }

    @Override
    public Single<List<MessageEntity>> getByCriteria(@NonNull MessagesCriteria criteria, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            final long start = System.currentTimeMillis();

            Cancelable cancelable = emitter::isDisposed;

            Cursor cursor = queryMessagesByCriteria(criteria);

            ArrayList<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    MessageEntity dbo = fullMapDbo(criteria.getAccountId(), cursor, withAtatchments, withForwardMessages, cancelable);

                    int position = dbos.size() - cursor.getPosition();
                    dbos.add(position, dbo);
                }

                cursor.close();
            }

            Exestime.log("MessagesStore.getByCriteria", start, "count: " + dbos.size());
            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<Integer> insert(int accountId, int peerId, @NonNull MessagePatch patch) {
        return Single.create(emitter -> {
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.PEER_ID, peerId);
            cv.put(MessageColumns.FROM_ID, patch.getSenderId());
            cv.put(MessageColumns.DATE, patch.getDate());
            cv.put(MessageColumns.READ_STATE, patch.isRead());
            cv.put(MessageColumns.OUT, patch.isOut());
            cv.put(MessageColumns.TITLE, patch.getTitle());
            cv.put(MessageColumns.BODY, patch.getBody());
            cv.put(MessageColumns.ENCRYPTED, patch.isEncrypted());
            cv.put(MessageColumns.IMPORTANT, patch.isImportant());
            cv.put(MessageColumns.DELETED, patch.isDeleted());
            cv.put(MessageColumns.FORWARD_COUNT, safeCountOf(patch.getForward()));
            cv.put(MessageColumns.HAS_ATTACHMENTS, nonEmpty(patch.getAttachments()));
            cv.put(MessageColumns.STATUS, patch.getStatus());
            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
            cv.put(MessageColumns.EXTRAS, isNull(patch.getExtras()) ? null : GSON.toJson(patch.getExtras()));

            // Other fileds is NULL

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri).withValues(cv);

            int index = addToListAndReturnIndex(operations, builder.build());

            if (nonEmpty(patch.getAttachments())) {
                List<Entity> entities = patch.getAttachments();

                for (Entity attachmentEntity : entities) {
                    AttachmentsStore.appendAttachOperationWithBackReference(operations, accountId, AttachToType.MESSAGE, index, attachmentEntity);
                }
            }

            if (nonEmpty(patch.getForward())) {
                for (MessageEntity fwdDbo : patch.getForward()) {
                    appendDboOperation(accountId, fwdDbo, operations, null, index);
                }
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            int resultMessageId = extractId(results[index]);
            emitter.onSuccess(resultMessageId);
        });
    }

    @Override
    public Single<Integer> applyPatch(int accountId, int messageId, @NonNull MessagePatch patch) {
        return getStores().attachments()
                .getCount(accountId, AttachToType.MESSAGE, messageId)
                .flatMap(count -> Single
                        .create(emitter -> {
                            final Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
                            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();

                            ContentValues cv = new ContentValues();
                            cv.put(MessageColumns.FROM_ID, patch.getSenderId());
                            cv.put(MessageColumns.DATE, patch.getDate());
                            cv.put(MessageColumns.READ_STATE, patch.isRead());
                            cv.put(MessageColumns.OUT, patch.isOut());
                            cv.put(MessageColumns.TITLE, patch.getTitle());
                            cv.put(MessageColumns.BODY, patch.getBody());
                            cv.put(MessageColumns.ENCRYPTED, patch.isEncrypted());
                            cv.put(MessageColumns.IMPORTANT, patch.isImportant());
                            cv.put(MessageColumns.DELETED, patch.isDeleted());
                            cv.put(MessageColumns.FORWARD_COUNT, safeCountOf(patch.getForward()));
                            cv.put(MessageColumns.HAS_ATTACHMENTS, count + safeCountOf(patch.getAttachments()) > 0);
                            cv.put(MessageColumns.STATUS, patch.getStatus());
                            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
                            cv.put(MessageColumns.EXTRAS, isNull(patch.getExtras()) ? null : GSON.toJson(patch.getExtras()));

                            final String where = MessageColumns._ID + " = ?";
                            final String[] args = {String.valueOf(messageId)};

                            operations.add(ContentProviderOperation.newUpdate(uri).withValues(cv).withSelection(where, args).build());

                            if (nonEmpty(patch.getAttachments())) {
                                for (Entity entity : patch.getAttachments()) {
                                    AttachmentsStore.appendAttachOperationWithStableAttachToId(operations, accountId, AttachToType.MESSAGE, messageId, entity);
                                }
                            }

                            if (nonEmpty(patch.getForward())) {
                                for (MessageEntity dbo : patch.getForward()) {
                                    appendDboOperation(accountId, dbo, operations, messageId, null);
                                }
                            }

                            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
                            emitter.onSuccess(messageId);
                        }));
    }

    private MessageEntity fullMapDbo(int accountId, Cursor cursor, boolean withAttachments, boolean withForwardMessages, @NonNull Cancelable cancelable) {
        final MessageEntity dbo = baseMapDbo(cursor);

        if (withAttachments && dbo.isHasAttachmens()) {
            List<Entity> attachments = getStores()
                    .attachments()
                    .getAttachmentsDbosSync(accountId, AttachToType.MESSAGE, dbo.getId(), cancelable);

            dbo.setAttachments(attachments);
        } else {
            dbo.setAttachments(Collections.emptyList());
        }

        if (withForwardMessages && dbo.getForwardCount() > 0) {
            List<MessageEntity> fwds = getForwardMessages(accountId, dbo.getId(), withAttachments, cancelable);
            dbo.setForwardMessages(fwds);
        } else {
            dbo.setForwardMessages(Collections.emptyList());
        }

        return dbo;
    }

    private static final Type EXTRAS_TYPE = new TypeToken<HashMap<Integer, String>>() {
    }.getType();

    private static MessageEntity baseMapDbo(Cursor cursor) {
        @MessageStatus
        int status = cursor.getInt(cursor.getColumnIndex(MessageColumns.STATUS));

        @ChatAction
        int action = cursor.getInt(cursor.getColumnIndex(MessageColumns.ACTION));

        final int id = cursor.getInt(cursor.getColumnIndex(MessageColumns._ID));
        final int peerId = cursor.getInt(cursor.getColumnIndex(MessageColumns.PEER_ID));
        final int fromId = cursor.getInt(cursor.getColumnIndex(MessageColumns.FROM_ID));

        HashMap<Integer, String> extras = null;

        String extrasText = cursor.getString(cursor.getColumnIndex(MessageColumns.EXTRAS));
        if (nonEmpty(extrasText)) {
            extras = GSON.fromJson(extrasText, EXTRAS_TYPE);
        }

        return new MessageEntity(id, peerId, fromId)
                .setEncrypted(cursor.getInt(cursor.getColumnIndex(MessageColumns.ENCRYPTED)) == 1)
                .setStatus(status)
                .setAction(action)
                .setExtras(extras)
                .setBody(cursor.getString(cursor.getColumnIndex(MessageColumns.BODY)))
                .setRead(cursor.getInt(cursor.getColumnIndex(MessageColumns.READ_STATE)) == 1)
                .setOut(cursor.getInt(cursor.getColumnIndex(MessageColumns.OUT)) == 1)
                .setStatus(status)
                .setDate(cursor.getLong(cursor.getColumnIndex(MessageColumns.DATE)))
                .setHasAttachmens(cursor.getInt(cursor.getColumnIndex(MessageColumns.HAS_ATTACHMENTS)) == 1)
                .setForwardCount(cursor.getInt(cursor.getColumnIndex(MessageColumns.FORWARD_COUNT)))
                .setDeleted(cursor.getInt(cursor.getColumnIndex(MessageColumns.DELETED)) == 1)
                .setTitle(cursor.getString(cursor.getColumnIndex(MessageColumns.TITLE)))
                .setOriginalId(cursor.getInt(cursor.getColumnIndex(MessageColumns.ORIGINAL_ID)))
                .setImportant(cursor.getInt(cursor.getColumnIndex(MessageColumns.IMPORTANT)) == 1)
                .setChatActive(cursor.getString(cursor.getColumnIndex(MessageColumns.CHAT_ACTIVE)))
                .setUsersCount(cursor.getInt(cursor.getColumnIndex(MessageColumns.USER_COUNT)))
                .setAdminId(cursor.getInt(cursor.getColumnIndex(MessageColumns.ADMIN_ID)))
                .setAction(action)
                .setActionMemberId(cursor.getInt(cursor.getColumnIndex(MessageColumns.ACTION_MID)))
                .setActionEmail(cursor.getString(cursor.getColumnIndex(MessageColumns.ACTION_EMAIL)))
                .setActionText(cursor.getString(cursor.getColumnIndex(MessageColumns.ACTION_TEXT)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(MessageColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(MessageColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(MessageColumns.PHOTO_200)))
                .setRandomId(cursor.getInt(cursor.getColumnIndex(MessageColumns.RANDOM_ID)));
    }

    @Override
    public Maybe<DraftMessage> findDraftMessage(int accountId, int peerId) {
        return Maybe.create(e -> {
            String[] columns = {MessageColumns._ID, MessageColumns.BODY};
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, columns,
                    MessageColumns.PEER_ID + " = ? AND " + MessageColumns.STATUS + " = ?",
                    new String[]{String.valueOf(peerId), String.valueOf(MessageStatus.EDITING)}, null);

            if (e.isDisposed()) return;

            DraftMessage message = null;
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MessageColumns._ID));
                    String body = cursor.getString(cursor.getColumnIndex(MessageColumns.BODY));
                    message = new DraftMessage(id, body);
                }

                cursor.close();
            }

            if (nonNull(message)) {
                Integer count = getStores().attachments()
                        .getCount(accountId, AttachToType.MESSAGE, message.getId())
                        .blockingGet();

                message.setAttachmentsCount(nonNull(count) ? count : 0);
                e.onSuccess(message);
            }

            e.onComplete();
        });
    }

    @Override
    public Single<Integer> saveDraftMessageBody(int accountId, int peerId, String body) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.BODY, body);
            cv.put(MessageColumns.PEER_ID, peerId);
            cv.put(MessageColumns.STATUS, MessageStatus.EDITING);

            ContentResolver cr = getContentResolver();

            Integer existDraftMessageId = findDraftMessageId(accountId, peerId);
            //.blockingGet();

            if (existDraftMessageId != null) {
                cr.update(uri, cv, MessageColumns._ID + " = ?", new String[]{String.valueOf(existDraftMessageId)});
            } else {
                Uri resultUri = cr.insert(uri, cv);
                existDraftMessageId = Integer.parseInt(resultUri.getLastPathSegment());
            }

            e.onSuccess(existDraftMessageId);

            Exestime.log("saveDraftMessageBody", start);
        });
    }

    @Override
    public Single<Integer> getMessageStatus(int accountId, int dbid) {
        return Single.fromCallable(() -> {
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getMessageContentUriFor(accountId),
                    new String[]{MessageColumns.STATUS}, MessageColumns.FULL_ID + " = ?", new String[]{String.valueOf(dbid)}, null);

            Integer result = null;

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    result = cursor.getInt(cursor.getColumnIndex(MessageColumns.STATUS));
                }

                cursor.close();
            }

            if (isNull(result)) {
                throw new RecordNotFoundException("Message with id " + dbid + " not found");
            }

            return result;
        });
    }

    private Integer findDraftMessageId(int accoutnId, int peerId) {
        String[] columns = {MessageColumns._ID};
        Uri uri = MessengerContentProvider.getMessageContentUriFor(accoutnId);

        Cursor cursor = getContext().getContentResolver().query(uri, columns,
                MessageColumns.PEER_ID + " = ? AND " + MessageColumns.STATUS + " = ?",
                new String[]{String.valueOf(peerId), String.valueOf(MessageStatus.EDITING)}, null);

        Integer id = null;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(MessageColumns._ID));
            }

            cursor.close();
        }

        return id;
    }

    @Override
    public Completable changeMessageStatus(int accountId, int messageId, @MessageStatus int status,
                                           @Nullable Integer vkid) {
        return Completable.create(e -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessageColumns.STATUS, status);
            if (vkid != null) {
                contentValues.put(MessageColumns._ID, vkid);
            }

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            int count = getContext().getContentResolver().update(uri, contentValues,
                    MessageColumns._ID + " = ?", new String[]{String.valueOf(messageId)});

            if (count > 0) {
                MessageUpdate update = new MessageUpdate(accountId, messageId);
                update.setStatusUpdate(new MessageUpdate.StatusUpdate(status));
                if (nonNull(vkid)) {
                    update.setSentUpdate(new MessageUpdate.SentUpdate(vkid));
                }

                messageUpdatePublishSubject.onNext(update);

                e.onComplete();
            } else {
                e.onError(new NotFoundException());
            }
        });
    }

    @Override
    public Single<Boolean> deleteMessage(int accountId, int messageId) {
        if (messageId == 0) {
            throw new IllegalArgumentException("Invalid message id: " + messageId);
        }

        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            int count = getContext().getContentResolver().delete(uri, MessageColumns._ID + " = ?",
                    new String[]{String.valueOf(messageId)});

            e.onSuccess(count > 0);
        });
    }

    private PublishSubject<MessageUpdate> messageUpdatePublishSubject = PublishSubject.create();

    @Override
    public Observable<MessageUpdate> observeMessageUpdates() {
        return messageUpdatePublishSubject;
    }

    @Override
    public Single<List<Integer>> getMissingMessages(int accountId, Collection<Integer> ids) {
        return Single.create(e -> {
            Set<Integer> copy = new HashSet<>(ids);

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            String[] projection = {MessageColumns._ID};
            String where = MessageColumns.FULL_ID + " IN(" + TextUtils.join(",", copy) + ")";
            Cursor cursor = getContentResolver().query(uri, projection, where, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(MessageColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(new ArrayList<>(copy));
        });
    }

    private int markAsRead(Uri uri, String where, String[] args) {
        ContentValues cv = new ContentValues();
        cv.put(MessageColumns.READ_STATE, true);
        return getContentResolver().update(uri, cv, where, args);
    }

    @Override
    public Completable markAsRead(int accountId, int peerId) {
        return Completable.fromAction(() -> {
            final Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            final String where = MessageColumns.PEER_ID + " = ? AND " + MessageColumns.OUT + " = ?";
            final String[] args = {String.valueOf(peerId), "0"};
            markAsRead(uri, where, args);
        });
    }

    private List<MessageEntity> getForwardMessages(int accountId, int attachTo, boolean withAttachments, @NonNull Cancelable cancelable) {
        Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
        String where = MessageColumns.ATTACH_TO + " = ?";
        String[] args = new String[]{String.valueOf(attachTo)};

        Cursor cursor = getContentResolver().query(uri, null, where, args, MessageColumns.FULL_ID + " DESC");

        List<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));

        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                MessageEntity dbo = this.fullMapDbo(accountId, cursor, withAttachments, true, cancelable);

                // Хз куда это еще влепить
                dbo.setRead(true);
                dbo.setOut(dbo.getFromId() == accountId);
                dbos.add(dbos.size() - cursor.getPosition(), dbo);
            }

            cursor.close();
        }

        return dbos;
    }

    @Override
    public Single<List<MessageEntity>> findMessagesByIds(int accountId, List<Integer> ids, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            String where;
            String[] args;

            if (ids.size() == 1) {
                where = MessageColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = MessageColumns.FULL_ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            Cancelable cancelable = emitter::isDisposed;

            ArrayList<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    MessageEntity dbo = fullMapDbo(accountId, cursor, withAtatchments, withForwardMessages, cancelable);

                    int position = dbos.size() - cursor.getPosition();
                    dbos.add(position, dbo);
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<Optional<Pair<Integer, MessageEntity>>> findFirstUnsentMessage(Collection<Integer> accountIds, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            final String where = MessageColumns.STATUS + " = ? OR " + MessageColumns.STATUS + " = ?";
            final String[] args = {String.valueOf(MessageStatus.QUEUE), String.valueOf(MessageStatus.SENDING)};
            final String orderBy = MessageColumns._ID + " ASC LIMIT 1";

            for (int accountId : accountIds) {
                if (emitter.isDisposed()) {
                    break;
                }

                Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

                Cursor cursor = getContentResolver().query(uri, null, where, args, orderBy);

                MessageEntity entity = null;

                if (nonNull(cursor)) {
                    if (cursor.moveToNext()) {
                        entity = fullMapDbo(accountId, cursor, withAtatchments, withForwardMessages, emitter::isDisposed);
                    }

                    cursor.close();
                }

                if (nonNull(entity)) {
                    emitter.onSuccess(Optional.wrap(Pair.create(accountId, entity)));
                    return;
                }
            }

            emitter.onSuccess(Optional.empty());
        });
    }

    @Override
    public Completable notifyMessageHasAttachments(int accountId, int messageId) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.HAS_ATTACHMENTS, true);
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            String where = MessageColumns._ID + " = ?";
            String[] args = {String.valueOf(messageId)};
            getContentResolver().update(uri, cv, where, args);
        });
    }

    @Override
    public Single<List<Integer>> getForwardMessageIds(int accountId, int attachTo) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver().query(uri,
                    new String[]{MessageColumns.ORIGINAL_ID}, MessageColumns.ATTACH_TO + " = ?",
                    new String[]{String.valueOf(attachTo)}, MessageColumns.FULL_ID + " DESC");

            ArrayList<Integer> ids = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    ids.add(cursor.getInt(cursor.getColumnIndex(MessageColumns.ORIGINAL_ID)));
                }

                cursor.close();
            }

            e.onSuccess(ids);
        });
    }
}

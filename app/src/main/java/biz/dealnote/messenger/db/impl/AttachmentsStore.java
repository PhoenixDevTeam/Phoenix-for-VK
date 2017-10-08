package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.AttachmentsColumns;
import biz.dealnote.messenger.db.column.CommentsAttachmentsColumns;
import biz.dealnote.messenger.db.column.PostAttachmentsColumns;
import biz.dealnote.messenger.db.interfaces.Cancelable;
import biz.dealnote.messenger.db.interfaces.IAttachmentsStore;
import biz.dealnote.messenger.db.model.AttachmentsTypes;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.exception.DatabaseException;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 15.11.2016.
 * phoenix
 */
class AttachmentsStore extends AbsStore implements IAttachmentsStore {

    AttachmentsStore(@NonNull AppStores base) {
        super(base);
    }

    private static Uri uriForType(@AttachToType int type, int accountId) {
        switch (type) {
            case AttachToType.COMMENT:
                return MessengerContentProvider.getCommentsAttachmentsContentUriFor(accountId);
            case AttachToType.MESSAGE:
                return MessengerContentProvider.getAttachmentsContentUriFor(accountId);
            case AttachToType.POST:
                return MessengerContentProvider.getPostsAttachmentsContentUriFor(accountId);
            default:
                throw new IllegalArgumentException();
        }
    }

    static void appendAttachOperationWithBackReference(@NonNull List<ContentProviderOperation> operations, int accountId,
                                                       @AttachToType int attachToType, int attachToBackReferenceIndex, @NonNull Entity entity) {
        ContentValues cv = new ContentValues();

        cv.put(typeColumnFor(attachToType), AttachmentsTypes.typeForInstance(entity));
        cv.put(dataColumnFor(attachToType), GSON.toJson(entity));

        operations.add(ContentProviderOperation.newInsert(uriForType(attachToType, accountId))
                .withValues(cv)
                .withValueBackReference(attachToIdColumnFor(attachToType), attachToBackReferenceIndex)
                .build());
    }

    static int appendAttachOperationWithStableAttachToId(@NonNull List<ContentProviderOperation> operations,
                                                         int accountId, @AttachToType int attachToType,
                                                         int attachToDbid, @NonNull Entity entity) throws DatabaseException {
        ContentValues cv = new ContentValues();
        cv.put(attachToIdColumnFor(attachToType), attachToDbid);
        cv.put(typeColumnFor(attachToType), AttachmentsTypes.typeForInstance(entity));
        cv.put(dataColumnFor(attachToType), serializeDbo(entity));

        return addToListAndReturnIndex(operations, ContentProviderOperation.newInsert(uriForType(attachToType, accountId))
                .withValues(cv)
                .build());
    }

    private static String idColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns._ID;
            case AttachToType.MESSAGE:
                return AttachmentsColumns._ID;
            case AttachToType.POST:
                return PostAttachmentsColumns._ID;
        }

        throw new IllegalArgumentException();
    }

    private static String attachToIdColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.C_ID;
            case AttachToType.MESSAGE:
                return AttachmentsColumns.MESSAGE_ID;
            case AttachToType.POST:
                return PostAttachmentsColumns.P_ID;
        }

        throw new IllegalArgumentException();
    }

    private static String typeColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.TYPE;
            case AttachToType.MESSAGE:
                return AttachmentsColumns.TYPE;
            case AttachToType.POST:
                return PostAttachmentsColumns.TYPE;
        }

        throw new IllegalArgumentException();
    }

    private static String dataColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.DATA;
            case AttachToType.MESSAGE:
                return AttachmentsColumns.DATA;
            case AttachToType.POST:
                return PostAttachmentsColumns.DATA;
        }

        throw new IllegalArgumentException();
    }

    private static String serializeDbo(Entity entity) {
        return GSON.toJson(entity);
    }

    private static Entity deserializeDbo(int type, String json) {
        Class<? extends Entity> dboClass = AttachmentsTypes.classForType(type);
        return GSON.fromJson(json, dboClass);
    }

    @Override
    public Single<int[]> attachDbos(int accountId, int attachToType, int attachToDbid, @NonNull List<Entity> entities) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(entities.size());

            int[] indexes = new int[entities.size()];
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                indexes[i] = appendAttachOperationWithStableAttachToId(operations, accountId, attachToType, attachToDbid, entity);
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int [] ids = new int[entities.size()];

            for (int i = 0; i < indexes.length; i++) {
                ContentProviderResult result = results[indexes[i]];
                int dbid = Integer.parseInt(result.uri.getPathSegments().get(1));
                ids[i] = dbid;
            }

            emitter.onSuccess(ids);
        });
    }

    @Override
    public Single<List<Pair<Integer, Entity>>> getAttachmentsDbosWithIds(int accountId, @AttachToType int attachToType, int attachToDbid) {
        return Single.create(emitter -> {
            Cursor cursor = createCursor(accountId, attachToType, attachToDbid);

            final List<Pair<Integer, Entity>> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    final int id = cursor.getInt(cursor.getColumnIndex(idColumnFor(attachToType)));
                    final int type = cursor.getInt(cursor.getColumnIndex(typeColumnFor(attachToType)));
                    final String json = cursor.getString(cursor.getColumnIndex(dataColumnFor(attachToType)));
                    final Entity entity = deserializeDbo(type, json);

                    dbos.add(Pair.create(id, entity));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    private Cursor createCursor(int accountId, int attachToType, int attachToDbid){
        Uri uri = uriForType(attachToType, accountId);
        return getContentResolver().query(uri, null,
                attachToIdColumnFor(attachToType) + " = ?", new String[]{String.valueOf(attachToDbid)}, null);
    }

    @Override
    public List<Entity> getAttachmentsDbosSync(int accountId, int attachToType, int attachToDbid, @NonNull Cancelable cancelable) {
        Cursor cursor = createCursor(accountId, attachToType, attachToDbid);

        final List<Entity> entities = new ArrayList<>(safeCountOf(cursor));

        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                int type = cursor.getInt(cursor.getColumnIndex(typeColumnFor(attachToType)));
                String json = cursor.getString(cursor.getColumnIndex(dataColumnFor(attachToType)));

                entities.add(deserializeDbo(type, json));
            }

            cursor.close();
        }

        return entities;
    }

    @Override
    public Completable remove(int accountId, @AttachToType int attachToType, int attachToDbid, int generatedAttachmentId) {
        return Completable.create(e -> {
            Uri uri = uriForType(attachToType, accountId);

            String selection = idColumnFor(attachToType) + " = ?";
            String[] args = {String.valueOf(generatedAttachmentId)};

            int count = getContext().getContentResolver().delete(uri, selection, args);

            if (count > 0) {
                e.onComplete();
            } else {
                e.onError(new NotFoundException());
            }
        });
    }

    @Override
    public Single<Integer> getCount(int accountId, int attachToType, int attachToDbid) {
        return Single.fromCallable(() -> {
            Uri uri = uriForType(attachToType, accountId);
            String selection = attachToIdColumnFor(attachToType) + " = ?";
            String[] args = {String.valueOf(attachToDbid)};

            Cursor cursor = getContentResolver().query(uri, null, selection, args, null);

            int count = safeCountOf(cursor);
            if (nonNull(cursor)) {
                cursor.close();
            }

            return count;
        });
    }
}

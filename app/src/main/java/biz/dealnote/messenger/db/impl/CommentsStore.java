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
import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.CommentsColumns;
import biz.dealnote.messenger.db.interfaces.Cancelable;
import biz.dealnote.messenger.db.interfaces.ICommentsStore;
import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.exception.DatabaseException;
import biz.dealnote.messenger.model.CommentUpdate;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.DraftComment;
import biz.dealnote.messenger.model.criteria.CommentsCriteria;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Unixtime;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

import static biz.dealnote.messenger.db.impl.AttachmentsStore.appendAttachOperationWithBackReference;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by ruslan.kolbasa on 28.11.2016.
 * phoenix
 */
class CommentsStore extends AbsStore implements ICommentsStore {

    private final PublishSubject<CommentUpdate> minorUpdatesPublisher;

    CommentsStore(@NonNull AppStores base) {
        super(base);
        this.minorUpdatesPublisher = PublishSubject.create();
    }

    private final Object mStoreLock = new Object();

    @Override
    public Single<int[]> insert(int accountId, int sourceId, int sourceOwnerId, int sourceType, List<CommentEntity> dbos, OwnerEntities owners, boolean clearBefore) {
        return Single.create(emitter -> {
            final ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearBefore) {
                ContentProviderOperation delete = ContentProviderOperation
                        .newDelete(MessengerContentProvider.getCommentsContentUriFor(accountId))
                        .withSelection(CommentsColumns.SOURCE_ID + " = ? " +
                                        " AND " + CommentsColumns.SOURCE_OWNER_ID + " = ? " +
                                        " AND " + CommentsColumns.COMMENT_ID + " != ? " +
                                        " AND " + CommentsColumns.SOURCE_TYPE + " = ?",
                                new String[]{String.valueOf(sourceId),
                                        String.valueOf(sourceOwnerId),
                                        String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                                        String.valueOf(sourceType)}).build();
                operations.add(delete);
            }

            int[] indexes = new int[dbos.size()];
            for (int i = 0; i < dbos.size(); i++) {
                CommentEntity dbo = dbos.get(i);

                ContentProviderOperation mainPostHeaderOperation = ContentProviderOperation
                        .newInsert(MessengerContentProvider.getCommentsContentUriFor(accountId))
                        .withValues(getCV(sourceId, sourceOwnerId, sourceType, dbo))
                        .build();

                int mainPostHeaderIndex = addToListAndReturnIndex(operations, mainPostHeaderOperation);
                indexes[i] = mainPostHeaderIndex;

                for (Entity attachmentEntity : dbo.getAttachments()) {
                    appendAttachOperationWithBackReference(operations, accountId, AttachToType.COMMENT, mainPostHeaderIndex, attachmentEntity);
                }
            }

            if (nonNull(owners)) {
                OwnersRepositiry.appendOwnersInsertOperations(operations, accountId, owners);
            }

            ContentProviderResult[] results;
            synchronized (mStoreLock) {
                results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            }

            final int[] ids = new int[dbos.size()];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];
                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            emitter.onSuccess(ids);
        });
    }

    public static ContentValues getCV(int sourceId, int sourceOwnerId, int sourceType, CommentEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(CommentsColumns.COMMENT_ID, dbo.getId());
        cv.put(CommentsColumns.FROM_ID, dbo.getFromId());
        cv.put(CommentsColumns.DATE, dbo.getDate());
        cv.put(CommentsColumns.TEXT, dbo.getText());
        cv.put(CommentsColumns.REPLY_TO_USER, dbo.getReplyToUserId());
        cv.put(CommentsColumns.REPLY_TO_COMMENT, dbo.getReplyToComment());
        cv.put(CommentsColumns.LIKES, dbo.getLikesCount());
        cv.put(CommentsColumns.USER_LIKES, dbo.isUserLikes());
        cv.put(CommentsColumns.CAN_LIKE, dbo.isCanLike());
        cv.put(CommentsColumns.ATTACHMENTS_COUNT, dbo.getAttachmentsCount());
        cv.put(CommentsColumns.SOURCE_ID, sourceId);
        cv.put(CommentsColumns.SOURCE_OWNER_ID, sourceOwnerId);
        cv.put(CommentsColumns.SOURCE_TYPE, sourceType);
        cv.put(CommentsColumns.DELETED, dbo.isDeleted());
        return cv;
    }

    private Cursor createCursorByCriteria(CommentsCriteria criteria) {
        Uri uri = MessengerContentProvider.getCommentsContentUriFor(criteria.getAccountId());

        DatabaseIdRange range = criteria.getRange();
        Commented commented = criteria.getCommented();

        if (Objects.isNull(range)) {
            return getContentResolver().query(uri, null,
                    CommentsColumns.SOURCE_ID + " = ? AND " +
                            CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                            CommentsColumns.SOURCE_TYPE + " = ? AND " +
                            CommentsColumns.COMMENT_ID + " != ?",
                    new String[]{String.valueOf(commented.getSourceId()),
                            String.valueOf(commented.getSourceOwnerId()),
                            String.valueOf(commented.getSourceType()),
                            String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID)},
                    CommentsColumns.COMMENT_ID + " DESC");
        } else {
            return getContentResolver().query(uri,
                    null, CommentsColumns._ID + " >= ? AND " + CommentsColumns._ID + " <= ?",
                    new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())},
                    CommentsColumns.COMMENT_ID + " DESC");
        }
    }

    @Override
    public Single<List<CommentEntity>> getDbosByCriteria(@NonNull CommentsCriteria criteria) {
        return Single.create(emitter -> {
            Cursor cursor = createCursorByCriteria(criteria);

            Cancelable cancelation = emitter::isDisposed;
            List<CommentEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapDbo(criteria.getAccountId(), cursor, true, false, cancelation));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Maybe<DraftComment> findEditingComment(int accountId, @NonNull Commented commented) {
        return Maybe.<DraftComment>create(e -> {
            Cursor cursor = getContentResolver().query(
                    MessengerContentProvider.getCommentsContentUriFor(accountId), null,
                    CommentsColumns.COMMENT_ID + " = ? AND " +
                            CommentsColumns.SOURCE_ID + " = ? AND " +
                            CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                            CommentsColumns.SOURCE_TYPE + " = ?",
                    new String[]{
                            String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                            String.valueOf(commented.getSourceId()),
                            String.valueOf(commented.getSourceOwnerId()),
                            String.valueOf(commented.getSourceType())}, null);

            DraftComment comment = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    int dbid = cursor.getInt(cursor.getColumnIndex(CommentsColumns._ID));
                    String body = cursor.getString(cursor.getColumnIndex(CommentsColumns.TEXT));

                    comment = new DraftComment(dbid).setBody(body);
                }

                cursor.close();
            }

            if (nonNull(comment)) {
                e.onSuccess(comment);
            }

            e.onComplete();
        }).flatMap(comment -> getStores()
                .attachments()
                .getCount(accountId, AttachToType.COMMENT, comment.getId())
                .flatMapMaybe(count -> Maybe.just(comment.setAttachmentsCount(count))));
    }

    @Override
    public Single<Integer> saveDraftComment(int accountId, Commented commented, String text, int replyToUser, int replyToComment) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Integer id = findEditingCommentId(accountId, commented);

            ContentValues contentValues = new ContentValues();
            contentValues.put(CommentsColumns.COMMENT_ID, CommentsColumns.PROCESSING_COMMENT_ID);
            contentValues.put(CommentsColumns.TEXT, text);
            contentValues.put(CommentsColumns.SOURCE_ID, commented.getSourceId());
            contentValues.put(CommentsColumns.SOURCE_OWNER_ID, commented.getSourceOwnerId());
            contentValues.put(CommentsColumns.SOURCE_TYPE, commented.getSourceType());
            contentValues.put(CommentsColumns.FROM_ID, accountId);
            contentValues.put(CommentsColumns.DATE, Unixtime.now());
            contentValues.put(CommentsColumns.REPLY_TO_USER, replyToUser);
            contentValues.put(CommentsColumns.REPLY_TO_COMMENT, replyToComment);
            contentValues.put(CommentsColumns.LIKES, 0);
            contentValues.put(CommentsColumns.USER_LIKES, 0);

            Uri commentsWithAccountUri = MessengerContentProvider.getCommentsContentUriFor(accountId);

            if (id == null) {
                Uri uri = getContentResolver().insert(commentsWithAccountUri, contentValues);

                if (uri == null) {
                    e.onError(new DatabaseException("Result URI is null"));
                    return;
                }

                id = Integer.parseInt(uri.getPathSegments().get(1));
            } else {
                getContentResolver().update(commentsWithAccountUri, contentValues,
                        CommentsColumns._ID + " = ?", new String[]{String.valueOf(id)});
            }

            e.onSuccess(id);
            Exestime.log("CommentsStore.saveDraftComment", start, "id: " + id);
        });
    }

    @Override
    public Completable commitMinorUpdate(CommentUpdate update) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();

            if (update.hasLikesUpdate()) {
                cv.put(CommentsColumns.USER_LIKES, update.getLikeUpdate().isUserLikes());
                cv.put(CommentsColumns.LIKES, update.getLikeUpdate().getCount());
            }

            if (update.hasDeleteUpdate()) {
                cv.put(CommentsColumns.DELETED, update.getDeleteUpdate().isDeleted());
            }

            Uri uri = MessengerContentProvider.getCommentsContentUriFor(update.getAccountId());

            String where = CommentsColumns.SOURCE_OWNER_ID + " = ? AND " + CommentsColumns.COMMENT_ID + " = ?";
            String[] args = {String.valueOf(update.getCommented().getSourceOwnerId()), String.valueOf(update.getCommentId())};

            getContentResolver().update(uri, cv, where, args);

            minorUpdatesPublisher.onNext(update);
        });
    }

    @Override
    public Observable<CommentUpdate> observeMinorUpdates() {
        return minorUpdatesPublisher;
    }

    @Override
    public Completable deleteByDbid(int accountId, Integer dbid) {
        return Completable.fromAction(() -> {
            Uri uri = MessengerContentProvider.getCommentsContentUriFor(accountId);
            String where = CommentsColumns._ID + " = ?";
            String[] args = {String.valueOf(dbid)};
            getContentResolver().delete(uri, where, args);
        });
    }

    private Integer findEditingCommentId(int aid, Commented commented) {
        String[] projection = {CommentsColumns._ID};
        Cursor cursor = getContentResolver().query(
                MessengerContentProvider.getCommentsContentUriFor(aid), projection,
                CommentsColumns.COMMENT_ID + " = ? AND " +
                        CommentsColumns.SOURCE_ID + " = ? AND " +
                        CommentsColumns.SOURCE_OWNER_ID + " = ? AND " +
                        CommentsColumns.SOURCE_TYPE + " = ?",
                new String[]{
                        String.valueOf(CommentsColumns.PROCESSING_COMMENT_ID),
                        String.valueOf(commented.getSourceId()),
                        String.valueOf(commented.getSourceOwnerId()),
                        String.valueOf(commented.getSourceType())}, null);

        Integer result = null;

        if (nonNull(cursor)) {
            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }

            cursor.close();
        }
        return result;
    }

    private CommentEntity mapDbo(int accountId, Cursor cursor, boolean includeAttachments, boolean forceAttachments, Cancelable cancelable) {
        int attachmentsCount = cursor.getInt(cursor.getColumnIndex(CommentsColumns.ATTACHMENTS_COUNT));
        int dbid = cursor.getInt(cursor.getColumnIndex(CommentsColumns._ID));

        int sourceId = cursor.getInt(cursor.getColumnIndex(CommentsColumns.SOURCE_ID));
        int sourceOwnerId = cursor.getInt(cursor.getColumnIndex(CommentsColumns.SOURCE_OWNER_ID));
        int sourceType = cursor.getInt(cursor.getColumnIndex(CommentsColumns.SOURCE_TYPE));
        String sourceAccessKey = cursor.getString(cursor.getColumnIndex(CommentsColumns.SOURCE_ACCESS_KEY));
        int id = cursor.getInt(cursor.getColumnIndex(CommentsColumns.COMMENT_ID));

        CommentEntity dbo = new CommentEntity(sourceId, sourceOwnerId, sourceType, sourceAccessKey, id)
                .setFromId(cursor.getInt(cursor.getColumnIndex(CommentsColumns.FROM_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndex(CommentsColumns.DATE)))
                .setText(cursor.getString(cursor.getColumnIndex(CommentsColumns.TEXT)))
                .setReplyToUserId(cursor.getInt(cursor.getColumnIndex(CommentsColumns.REPLY_TO_USER)))
                .setReplyToComment(cursor.getInt(cursor.getColumnIndex(CommentsColumns.REPLY_TO_COMMENT)))
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(CommentsColumns.LIKES)))
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(CommentsColumns.USER_LIKES)) == 1)
                .setCanLike(cursor.getInt(cursor.getColumnIndex(CommentsColumns.CAN_LIKE)) == 1)
                .setCanEdit(cursor.getInt(cursor.getColumnIndex(CommentsColumns.CAN_EDIT)) == 1)
                .setDeleted(cursor.getInt(cursor.getColumnIndex(CommentsColumns.DELETED)) == 1);

        if (includeAttachments && (attachmentsCount > 0 || forceAttachments)) {
            dbo.setAttachments(getStores()
                    .attachments()
                    .getAttachmentsDbosSync(accountId, AttachToType.COMMENT, dbid, cancelable));
        }

        return dbo;
    }
}
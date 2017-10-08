package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.FeedListsColumns;
import biz.dealnote.messenger.db.column.NewsColumns;
import biz.dealnote.messenger.db.interfaces.IFeedStore;
import biz.dealnote.messenger.db.model.entity.AttachmentsEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.FeedListEntity;
import biz.dealnote.messenger.db.model.entity.NewsEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.model.FeedSourceCriteria;
import biz.dealnote.messenger.model.criteria.FeedCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.join;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

class FeedStore extends AbsStore implements IFeedStore {

    FeedStore(@NonNull AppStores base) {
        super(base);
    }

    private final Object storeLock = new Object();

    @Override
    public Single<List<NewsEntity>> findByCriteria(@NonNull FeedCriteria criteria) {
        return Single.create(e -> {
            final Uri uri = MessengerContentProvider.getNewsContentUriFor(criteria.getAccountId());
            final List<NewsEntity> data = new ArrayList<>();

            synchronized (storeLock) {
                Cursor cursor;
                if (criteria.getRange() != null) {
                    DatabaseIdRange range = criteria.getRange();
                    cursor = getContext().getContentResolver().query(uri, null,
                            NewsColumns._ID + " >= ? AND " + NewsColumns._ID + " <= ?",
                            new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())}, null);
                } else {
                    cursor = getContext().getContentResolver().query(uri, null, null, null, null);
                }

                if (nonNull(cursor)) {
                    while (cursor.moveToNext()) {
                        if (e.isDisposed()) {
                            break;
                        }

                        data.add(mapNewsBase(cursor));
                    }

                    cursor.close();
                }
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Single<int[]> store(int accountId, @NonNull List<NewsEntity> dbos, @Nullable OwnerEntities owners, boolean clearBeforeStore) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getNewsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeStore) {
                // for performance test (before - 500-600ms, after - 200-300ms)
                //operations.add(ContentProviderOperation.newDelete(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                //        .build());

                operations.add(ContentProviderOperation.newDelete(uri).build());
            }

            int[] indexes = new int[dbos.size()];
            for (int i = 0; i < dbos.size(); i++) {
                NewsEntity dbo = dbos.get(i);

                ContentValues cv = getCV(dbo);

                ContentProviderOperation mainPostHeaderOperation = ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build();

                int mainPostHeaderIndex = addToListAndReturnIndex(operations, mainPostHeaderOperation);
                indexes[i] = mainPostHeaderIndex;
            }

            if (nonNull(owners)) {
                OwnersRepositiry.appendOwnersInsertOperations(operations, accountId, owners);
            }

            ContentProviderResult[] results;

            synchronized (storeLock) {
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

    public static ContentValues getCV(NewsEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(NewsColumns.TYPE, dbo.getType());
        cv.put(NewsColumns.SOURCE_ID, dbo.getSourceId());
        cv.put(NewsColumns.DATE, dbo.getDate());
        cv.put(NewsColumns.POST_ID, dbo.getPostId());
        cv.put(NewsColumns.POST_TYPE, dbo.getPostType());
        cv.put(NewsColumns.FINAL_POST, dbo.isFinalPost());
        cv.put(NewsColumns.COPY_OWNER_ID, dbo.getCopyOwnerId());
        cv.put(NewsColumns.COPY_POST_ID, dbo.getCopyPostId());
        cv.put(NewsColumns.COPY_POST_DATE, dbo.getCopyPostDate());
        cv.put(NewsColumns.TEXT, dbo.getText());
        cv.put(NewsColumns.CAN_EDIT, dbo.isCanEdit());
        cv.put(NewsColumns.CAN_DELETE, dbo.isCanDelete());
        cv.put(NewsColumns.COMMENT_COUNT, dbo.getCommentCount());
        cv.put(NewsColumns.COMMENT_CAN_POST, dbo.isCanPostComment());
        cv.put(NewsColumns.LIKE_COUNT, dbo.getLikesCount());
        cv.put(NewsColumns.USER_LIKE, dbo.isUserLikes());
        cv.put(NewsColumns.CAN_LIKE, dbo.isCanLike());
        cv.put(NewsColumns.CAN_PUBLISH, dbo.isCanPublish());
        cv.put(NewsColumns.REPOSTS_COUNT, dbo.getRepostCount());
        cv.put(NewsColumns.USER_REPOSTED, dbo.isUserReposted());
        cv.put(NewsColumns.GEO_ID, dbo.getGeoId());
        cv.put(NewsColumns.TAG_FRIENDS, nonNull(dbo.getFriendsTags()) ? join(",", dbo.getFriendsTags()) : null);
        cv.put(NewsColumns.VIEWS, dbo.getViews());

        if (nonEmpty(dbo.getCopyHistory()) || nonEmpty(dbo.getAttachments())) {
            List<Entity> attachmentsEntities = new ArrayList<>();

            if (nonEmpty(dbo.getAttachments())) {
                attachmentsEntities.addAll(dbo.getAttachments());
            }

            if (nonEmpty(dbo.getCopyHistory())) {
                attachmentsEntities.addAll(dbo.getCopyHistory());
            }

            if (nonEmpty(attachmentsEntities)) {
                AttachmentsEntity attachmentsEntity = new AttachmentsEntity(attachmentsEntities);
                cv.put(NewsColumns.ATTACHMENTS_JSON, GSON.toJson(attachmentsEntity));
            } else {
                cv.putNull(NewsColumns.ATTACHMENTS_JSON);
            }
        }

        return cv;
    }

    @Override
    public Completable storeLists(int accountid, @NonNull List<FeedListEntity> entities) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFeedListsContentUriFor(accountid);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            operations.add(ContentProviderOperation.newDelete(uri)
                    .build());

            for (FeedListEntity entity : entities) {
                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(FeedListsColumns.getCV(entity))
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    @Override
    public Single<List<FeedListEntity>> getAllLists(@NonNull FeedSourceCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getFeedListsContentUriFor(criteria.getAccountId());
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            List<FeedListEntity> data = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    data.add(mapList(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    private static FeedListEntity mapList(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(FeedListsColumns._ID));
        String title = cursor.getString(cursor.getColumnIndex(FeedListsColumns.TITLE));

        FeedListEntity entity = new FeedListEntity(id).setTitle(title);

        String sources = cursor.getString(cursor.getColumnIndex(FeedListsColumns.SOURCE_IDS));

        int[] sourceIds = null;

        if (nonEmpty(sources)) {
            String[] ids = sources.split(",");

            sourceIds = new int[ids.length];

            for (int i = 0; i < ids.length; i++) {
                sourceIds[i] = Integer.parseInt(ids[i]);
            }
        }

        return entity.setSourceIds(sourceIds)
                .setNoReposts(cursor.getInt(cursor.getColumnIndex(FeedListsColumns.NO_REPOSTS)) == 1);
    }

    /*private void fillAttachmentsOperations(int accountId, @NonNull VKApiAttachment attachment, @NonNull List<ContentProviderOperation> target,
                                           int parentPostHeaderOperationIndex) {
        Logger.d("fillAttachmentsOperations", "attachment: " + attachment.toAttachmentString());

        ContentValues cv = new ContentValues();
        cv.put(NewsAttachmentsColumns.TYPE, Types.from(attachment.getType()));
        cv.put(NewsAttachmentsColumns.DATA, serializeAttachment(attachment));

        target.add(ContentProviderOperation.newInsert(MessengerContentProvider.getNewsAttachmentsContentUriFor(accountId))
                .withValues(cv)
                .withValueBackReference(NewsAttachmentsColumns.N_ID, parentPostHeaderOperationIndex)
                .build());
    }*/

    private NewsEntity mapNewsBase(Cursor cursor) {
        String friendString = cursor.getString(cursor.getColumnIndex(NewsColumns.TAG_FRIENDS));

        ArrayList<String> friends = null;
        if (nonEmpty(friendString)) {
            friends = new ArrayList<>();
            friends.addAll(Arrays.asList(friendString.split(",")));
        }

        NewsEntity dbo = new NewsEntity()
                .setFriendsTags(friends)
                .setType(cursor.getString(cursor.getColumnIndex(NewsColumns.TYPE)))
                .setSourceId(cursor.getInt(cursor.getColumnIndex(NewsColumns.SOURCE_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndex(NewsColumns.DATE)))
                .setPostId(cursor.getInt(cursor.getColumnIndex(NewsColumns.POST_ID)))
                .setPostType(cursor.getString(cursor.getColumnIndex(NewsColumns.POST_TYPE)))
                .setFinalPost(cursor.getInt(cursor.getColumnIndex(NewsColumns.FINAL_POST)) == 1)
                .setCopyOwnerId(cursor.getInt(cursor.getColumnIndex(NewsColumns.COPY_OWNER_ID)))
                .setCopyPostId(cursor.getInt(cursor.getColumnIndex(NewsColumns.COPY_POST_ID)))
                .setCopyPostDate(cursor.getLong(cursor.getColumnIndex(NewsColumns.COPY_POST_DATE)))
                .setText(cursor.getString(cursor.getColumnIndex(NewsColumns.TEXT)))
                .setCanEdit(cursor.getInt(cursor.getColumnIndex(NewsColumns.CAN_EDIT)) == 1)
                .setCanDelete(cursor.getInt(cursor.getColumnIndex(NewsColumns.CAN_DELETE)) == 1)
                .setCommentCount(cursor.getInt(cursor.getColumnIndex(NewsColumns.COMMENT_COUNT)))
                .setCanPostComment(cursor.getInt(cursor.getColumnIndex(NewsColumns.COMMENT_CAN_POST)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(NewsColumns.LIKE_COUNT)))
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(NewsColumns.USER_LIKE)) == 1)
                .setCanLike(cursor.getInt(cursor.getColumnIndex(NewsColumns.CAN_LIKE)) == 1)
                .setCanPublish(cursor.getInt(cursor.getColumnIndex(NewsColumns.CAN_PUBLISH)) == 1)
                .setRepostCount(cursor.getInt(cursor.getColumnIndex(NewsColumns.REPOSTS_COUNT)))
                .setUserReposted(cursor.getInt(cursor.getColumnIndex(NewsColumns.USER_REPOSTED)) == 1)
                .setViews(cursor.getInt(cursor.getColumnIndex(NewsColumns.VIEWS)));

        String attachmentsJson = cursor.getString(cursor.getColumnIndex(NewsColumns.ATTACHMENTS_JSON));

        if (nonEmpty(attachmentsJson)) {
            AttachmentsEntity attachmentsEntity = GSON.fromJson(attachmentsJson, AttachmentsEntity.class);

            List<Entity> all = attachmentsEntity.getEntities();

            List<Entity> attachmentsOnly = new ArrayList<>(all.size());
            List<PostEntity> copiesOnly = new ArrayList<>(0);

            for (Entity a : all) {
                if (a instanceof PostEntity) {
                    copiesOnly.add((PostEntity) a);
                } else {
                    attachmentsOnly.add(a);
                }
            }

            dbo.setAttachments(attachmentsOnly);
            dbo.setCopyHistory(copiesOnly);
        } else {
            dbo.setCopyHistory(Collections.emptyList());
            dbo.setAttachments(Collections.emptyList());
        }

        return dbo;
    }
}
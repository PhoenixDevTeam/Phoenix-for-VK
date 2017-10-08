package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.TopicsColumns;
import biz.dealnote.messenger.db.interfaces.ITopicsStore;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PollEntity;
import biz.dealnote.messenger.db.model.entity.TopicEntity;
import biz.dealnote.messenger.model.criteria.TopicsCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 13.12.2016.
 * phoenix
 */
class TopicsStore extends AbsStore implements ITopicsStore {

    TopicsStore(@NonNull AppStores base) {
        super(base);
    }

    @Override
    public Single<List<TopicEntity>> getByCriteria(@NonNull TopicsCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getTopicsContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            if (nonNull(criteria.getRange())) {
                DatabaseIdRange range = criteria.getRange();
                where = TopicsColumns._ID + " >= ? AND " + TopicsColumns._ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else {
                where = TopicsColumns.OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            ArrayList<TopicEntity> topics = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if(e.isDisposed()){
                        break;
                    }

                    topics.add(mapDbo(cursor));
                }

                cursor.close();
            }

            e.onSuccess(topics);
        });
    }

    @Override
    public Completable store(int accountId, int ownerId, List<TopicEntity> topics, OwnerEntities owners, boolean canAddTopic, int defaultOrder, boolean clearBefore) {
        return Completable.create(e -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            Uri uri = MessengerContentProvider.getTopicsContentUriFor(accountId);

            if (nonNull(owners)) {
                OwnersRepositiry.appendOwnersInsertOperations(operations, accountId, owners);
            }

            if (clearBefore) {
                operations.add(ContentProviderOperation.newDelete(uri)
                        .withSelection(TopicsColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                        .build());
            }

            for (TopicEntity dbo : topics) {
                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(getCV(dbo))
                        .build());
            }

            ContentValues cv = new ContentValues();
            cv.put(GroupColumns.CAN_ADD_TOPICS, canAddTopic);
            cv.put(GroupColumns.TOPICS_ORDER, defaultOrder);

            operations.add(ContentProviderOperation
                    .newUpdate(MessengerContentProvider.getGroupsContentUriFor(accountId))
                    .withValues(cv)
                    .withSelection(GroupColumns._ID + " = ?", new String[]{String.valueOf(Math.abs(ownerId))})
                    .build());

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    public static ContentValues getCV(TopicEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(TopicsColumns.TOPIC_ID, dbo.getId());
        cv.put(TopicsColumns.OWNER_ID, dbo.getOwnerId());
        cv.put(TopicsColumns.TITLE, dbo.getTitle());
        cv.put(TopicsColumns.CREATED, dbo.getCreatedTime());
        cv.put(TopicsColumns.CREATED_BY, dbo.getCreatorId());
        cv.put(TopicsColumns.UPDATED, dbo.getLastUpdateTime());
        cv.put(TopicsColumns.UPDATED_BY, dbo.getUpdatedBy());
        cv.put(TopicsColumns.IS_CLOSED, dbo.isClosed());
        cv.put(TopicsColumns.IS_FIXED, dbo.isFixed());
        cv.put(TopicsColumns.COMMENTS, dbo.getCommentsCount());
        cv.put(TopicsColumns.FIRST_COMMENT, dbo.getFirstComment());
        cv.put(TopicsColumns.LAST_COMMENT, dbo.getLastComment());
        cv.put(TopicsColumns.ATTACHED_POLL, nonNull(dbo.getPoll()) ? GSON.toJson(dbo.getPoll()) : null);
        return cv;
    }

    @Override
    public Completable attachPoll(int accountId, int ownerId, int topicId, PollEntity dbo) {
        return Completable.create(e -> {
            ContentValues cv = new ContentValues();
            cv.put(TopicsColumns.ATTACHED_POLL, nonNull(dbo) ? GSON.toJson(dbo) : null);

            Uri uri = MessengerContentProvider.getTopicsContentUriFor(accountId);

            String where = TopicsColumns.TOPIC_ID + " = ? AND " + TopicsColumns.OWNER_ID + " = ?";
            String[] args = {String.valueOf(topicId), String.valueOf(topicId)};

            getContentResolver().update(uri, cv, where, args);
            e.onComplete();
        });
    }

    private static TopicEntity mapDbo(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(TopicsColumns.TOPIC_ID));
        int ownerId = cursor.getInt(cursor.getColumnIndex(TopicsColumns.OWNER_ID));

        TopicEntity dbo = new TopicEntity(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndex(TopicsColumns.TITLE)))
                .setCreatedTime(cursor.getLong(cursor.getColumnIndex(TopicsColumns.CREATED)))
                .setCreatorId(cursor.getInt(cursor.getColumnIndex(TopicsColumns.CREATED_BY)))
                .setLastUpdateTime(cursor.getLong(cursor.getColumnIndex(TopicsColumns.UPDATED)))
                .setUpdatedBy(cursor.getInt(cursor.getColumnIndex(TopicsColumns.UPDATED_BY)))
                .setClosed(cursor.getInt(cursor.getColumnIndex(TopicsColumns.IS_CLOSED)) == 1)
                .setFixed(cursor.getInt(cursor.getColumnIndex(TopicsColumns.IS_FIXED)) == 1)
                .setCommentsCount(cursor.getInt(cursor.getColumnIndex(TopicsColumns.COMMENTS)))
                .setFirstComment(cursor.getString(cursor.getColumnIndex(TopicsColumns.FIRST_COMMENT)))
                .setLastComment(cursor.getString(cursor.getColumnIndex(TopicsColumns.LAST_COMMENT)));

        String pollJson = cursor.getString(cursor.getColumnIndex(TopicsColumns.ATTACHED_POLL));
        if (nonEmpty(pollJson)) {
            dbo.setPoll(GSON.fromJson(pollJson, PollEntity.class));
        }

        return dbo;
    }
}
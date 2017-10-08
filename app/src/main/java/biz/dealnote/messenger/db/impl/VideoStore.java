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
import biz.dealnote.messenger.db.column.VideoColumns;
import biz.dealnote.messenger.db.interfaces.IVideoStore;
import biz.dealnote.messenger.db.model.entity.PrivacyEntity;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.VideoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

import static android.provider.BaseColumns._ID;
import static biz.dealnote.messenger.db.column.VideoColumns.ACCESS_KEY;
import static biz.dealnote.messenger.db.column.VideoColumns.ADDING_DATE;
import static biz.dealnote.messenger.db.column.VideoColumns.ALBUM_ID;
import static biz.dealnote.messenger.db.column.VideoColumns.CAN_COMENT;
import static biz.dealnote.messenger.db.column.VideoColumns.CAN_REPOST;
import static biz.dealnote.messenger.db.column.VideoColumns.COMMENTS;
import static biz.dealnote.messenger.db.column.VideoColumns.DATE;
import static biz.dealnote.messenger.db.column.VideoColumns.DESCRIPTION;
import static biz.dealnote.messenger.db.column.VideoColumns.DURATION;
import static biz.dealnote.messenger.db.column.VideoColumns.EXTERNAL;
import static biz.dealnote.messenger.db.column.VideoColumns.LIKES;
import static biz.dealnote.messenger.db.column.VideoColumns.LINK;
import static biz.dealnote.messenger.db.column.VideoColumns.MP4_1080;
import static biz.dealnote.messenger.db.column.VideoColumns.MP4_240;
import static biz.dealnote.messenger.db.column.VideoColumns.MP4_360;
import static biz.dealnote.messenger.db.column.VideoColumns.MP4_480;
import static biz.dealnote.messenger.db.column.VideoColumns.MP4_720;
import static biz.dealnote.messenger.db.column.VideoColumns.ORIGINAL_OWNER_ID;
import static biz.dealnote.messenger.db.column.VideoColumns.OWNER_ID;
import static biz.dealnote.messenger.db.column.VideoColumns.PHOTO_130;
import static biz.dealnote.messenger.db.column.VideoColumns.PHOTO_320;
import static biz.dealnote.messenger.db.column.VideoColumns.PHOTO_800;
import static biz.dealnote.messenger.db.column.VideoColumns.PLAYER;
import static biz.dealnote.messenger.db.column.VideoColumns.PRIVACY_COMMENT;
import static biz.dealnote.messenger.db.column.VideoColumns.PRIVACY_VIEW;
import static biz.dealnote.messenger.db.column.VideoColumns.REPEAT;
import static biz.dealnote.messenger.db.column.VideoColumns.TITLE;
import static biz.dealnote.messenger.db.column.VideoColumns.USER_LIKES;
import static biz.dealnote.messenger.db.column.VideoColumns.VIDEO_ID;
import static biz.dealnote.messenger.db.column.VideoColumns.VIEWS;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
class VideoStore extends AbsStore implements IVideoStore {

    VideoStore(@NonNull AppStores base) {
        super(base);
    }

    @Override
    public Single<List<VideoEntity>> findByCriteria(@NonNull VideoCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getVideosContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();
            if(nonNull(range)){
                where = _ID + " >= ? AND " + _ID + " <= ?";
                args = new String[]{String.valueOf(range.getFirst()), String.valueOf(range.getLast())};
            } else if(criteria.getAlbumId() == 0){
                where = OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            } else {
                where = OWNER_ID + " = ? AND " + ALBUM_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId()), String.valueOf(criteria.getAlbumId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, ADDING_DATE + " DESC");

            ArrayList<VideoEntity> videos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    videos.add(mapVideo(cursor));
                }

                cursor.close();
            }

            e.onSuccess(videos);
        });
    }

    private VideoEntity mapVideo(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(VIDEO_ID));
        final int ownerId = cursor.getInt(cursor.getColumnIndex(ORIGINAL_OWNER_ID));

        VideoEntity video = new VideoEntity(id, ownerId)
                .setAlbumId(cursor.getInt(cursor.getColumnIndex(ALBUM_ID)))
                .setTitle(cursor.getString(cursor.getColumnIndex(TITLE)))
                .setDescription(cursor.getString(cursor.getColumnIndex(DESCRIPTION)))
                .setLink(cursor.getString(cursor.getColumnIndex(LINK)))
                .setDuration(cursor.getInt(cursor.getColumnIndex(DURATION)))
                .setDate(cursor.getLong(cursor.getColumnIndex(DATE)))
                .setAddingDate(cursor.getLong(cursor.getColumnIndex(ADDING_DATE)))
                .setViews(cursor.getInt(cursor.getColumnIndex(VIEWS)))
                .setPlayer(cursor.getString(cursor.getColumnIndex(PLAYER)))
                .setPhoto130(cursor.getString(cursor.getColumnIndex(PHOTO_130)))
                .setPhoto320(cursor.getString(cursor.getColumnIndex(PHOTO_320)))
                .setPhoto800(cursor.getString(cursor.getColumnIndex(PHOTO_800)))
                .setAccessKey(cursor.getString(cursor.getColumnIndex(ACCESS_KEY)))
                .setCommentsCount(cursor.getInt(cursor.getColumnIndex(COMMENTS)))
                .setCanComment(cursor.getInt(cursor.getColumnIndex(CAN_COMENT)) == 1)
                .setCanRepost(cursor.getInt(cursor.getColumnIndex(CAN_REPOST)) == 1)
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(USER_LIKES)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(LIKES)))
                .setRepeat(cursor.getInt(cursor.getColumnIndex(REPEAT)) == 1)
                .setMp4link240(cursor.getString(cursor.getColumnIndex(MP4_240)))
                .setMp4link360(cursor.getString(cursor.getColumnIndex(MP4_360)))
                .setMp4link480(cursor.getString(cursor.getColumnIndex(MP4_480)))
                .setMp4link720(cursor.getString(cursor.getColumnIndex(MP4_720)))
                .setMp4link1080(cursor.getString(cursor.getColumnIndex(MP4_1080)))
                .setExternalLink(cursor.getString(cursor.getColumnIndex(EXTERNAL)))
                .setPlatform(cursor.getString(cursor.getColumnIndex(VideoColumns.PLATFORM)))
                .setCanEdit(cursor.getInt(cursor.getColumnIndex(VideoColumns.CAN_EDIT)) == 1)
                .setCanAdd(cursor.getInt(cursor.getColumnIndex(VideoColumns.CAN_ADD)) == 1);

        String privacyViewText = cursor.getString(cursor.getColumnIndex(PRIVACY_VIEW));
        if(nonEmpty(privacyViewText)){
            video.setPrivacyView(GSON.fromJson(privacyViewText, PrivacyEntity.class));
        }

        String privacyCommentText = cursor.getString(cursor.getColumnIndex(PRIVACY_COMMENT));
        if(nonEmpty(privacyCommentText)){
            video.setPrivacyComment(GSON.fromJson(privacyCommentText, PrivacyEntity.class));
        }

        return video;
    }

    /* Дело в том, что вк передает в p.owner_id идентификатор оригинального владельца.
     * Поэтому необходимо отдельно сохранять идентикатор owner-а, у кого в видеозаписях мы нашли видео */
    public static ContentValues getCV(VideoEntity dbo, int ownerId){
        ContentValues cv = new ContentValues();
        cv.put(VideoColumns.VIDEO_ID, dbo.getId());
        cv.put(VideoColumns.OWNER_ID, ownerId);
        cv.put(VideoColumns.ORIGINAL_OWNER_ID, dbo.getOwnerId());
        cv.put(VideoColumns.ALBUM_ID, dbo.getAlbumId());
        cv.put(VideoColumns.TITLE, dbo.getTitle());
        cv.put(VideoColumns.DESCRIPTION, dbo.getDescription());
        cv.put(VideoColumns.DURATION, dbo.getDuration());
        cv.put(VideoColumns.LINK, dbo.getLink());
        cv.put(VideoColumns.DATE, dbo.getDate());
        cv.put(VideoColumns.ADDING_DATE, dbo.getAddingDate());
        cv.put(VideoColumns.VIEWS, dbo.getViews());
        cv.put(VideoColumns.PLAYER, dbo.getPlayer());
        cv.put(VideoColumns.PHOTO_130, dbo.getPhoto130());
        cv.put(VideoColumns.PHOTO_320, dbo.getPhoto320());
        cv.put(VideoColumns.PHOTO_800, dbo.getPhoto800());
        cv.put(VideoColumns.ACCESS_KEY, dbo.getAccessKey());
        cv.put(VideoColumns.COMMENTS, dbo.getCommentsCount());
        cv.put(VideoColumns.CAN_COMENT, dbo.isCanComment());
        cv.put(VideoColumns.CAN_REPOST, dbo.isCanRepost());
        cv.put(VideoColumns.USER_LIKES, dbo.isUserLikes());
        cv.put(VideoColumns.REPEAT, dbo.isRepeat());
        cv.put(VideoColumns.LIKES, dbo.getLikesCount());
        cv.put(VideoColumns.PRIVACY_VIEW, nonNull(dbo.getPrivacyView()) ? GSON.toJson(dbo.getPrivacyView()) : null);
        cv.put(VideoColumns.PRIVACY_COMMENT, nonNull(dbo.getPrivacyComment()) ? GSON.toJson(dbo.getPrivacyComment()) : null);
        cv.put(VideoColumns.MP4_240, dbo.getMp4link240());
        cv.put(VideoColumns.MP4_360, dbo.getMp4link360());
        cv.put(VideoColumns.MP4_480, dbo.getMp4link480());
        cv.put(VideoColumns.MP4_720, dbo.getMp4link720());
        cv.put(VideoColumns.MP4_1080, dbo.getMp4link1080());
        cv.put(VideoColumns.EXTERNAL, dbo.getExternalLink());
        cv.put(VideoColumns.PLATFORM, dbo.getPlatform());
        cv.put(VideoColumns.CAN_EDIT, dbo.isCanEdit());
        cv.put(VideoColumns.CAN_ADD, dbo.isCanAdd());
        return cv;
    }

    @Override
    public Completable insertData(int accountId, int ownerId, int albumId, List<VideoEntity> videos, boolean deleteBeforeInsert) {
        return Completable.create(e -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            Uri uri = MessengerContentProvider.getVideosContentUriFor(accountId);

            if (deleteBeforeInsert) {
                if (albumId == 0) {
                    operations.add(ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(VideoColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                            .build());
                } else {
                    operations.add(ContentProviderOperation
                            .newDelete(uri)
                            .withSelection(VideoColumns.OWNER_ID + " = ? AND " + VideoColumns.ALBUM_ID + " = ?",
                                    new String[]{String.valueOf(ownerId), String.valueOf(albumId)})
                            .build());
                }
            }

            for (VideoEntity dbo : videos) {
                ContentValues cv = getCV(dbo, ownerId);
                cv.put(VideoColumns.ALBUM_ID, albumId);

                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }
}

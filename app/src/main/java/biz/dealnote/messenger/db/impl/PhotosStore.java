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
import biz.dealnote.messenger.db.column.PhotosColumns;
import biz.dealnote.messenger.db.interfaces.IPhotosStore;
import biz.dealnote.messenger.db.model.PhotoPatch;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.db.model.entity.PhotoSizeEntity;
import biz.dealnote.messenger.model.criteria.PhotoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

class PhotosStore extends AbsStore implements IPhotosStore {

    PhotosStore(@NonNull AppStores base) {
        super(base);
    }

    @Override
    public Completable insertPhotosRx(int accountId, int ownerId, int albumId, @NonNull List<PhotoEntity> photos, boolean clearBeforeInsert) {
        return Completable.fromAction(() -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(clearBeforeInsert ? photos.size() + 1 : photos.size());
            Uri uri = MessengerContentProvider.getPhotosContentUriFor(accountId);

            if(clearBeforeInsert){
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(PhotosColumns.OWNER_ID + " = ? AND " + PhotosColumns.ALBUM_ID + " = ?"
                                , new String[]{String.valueOf(ownerId), String.valueOf(albumId)})
                        .build());
            }

            for(PhotoEntity dbo : photos){
                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(getCV(dbo))
                        .build());
            }

            getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
        });
    }

    private static ContentValues getCV(PhotoEntity dbo){
        ContentValues cv = new ContentValues();
        cv.put(PhotosColumns.PHOTO_ID, dbo.getId());
        cv.put(PhotosColumns.ALBUM_ID, dbo.getAlbumId());
        cv.put(PhotosColumns.OWNER_ID, dbo.getOwnerId());
        cv.put(PhotosColumns.WIDTH, dbo.getWidth());
        cv.put(PhotosColumns.HEIGHT, dbo.getHeight());
        cv.put(PhotosColumns.TEXT, dbo.getText());
        cv.put(PhotosColumns.DATE, dbo.getDate());

        if(nonNull(dbo.getSizes())){
            cv.put(PhotosColumns.SIZES, GSON.toJson(dbo.getSizes()));
        }

        cv.put(PhotosColumns.USER_LIKES, dbo.isUserLikes());
        cv.put(PhotosColumns.CAN_COMMENT, dbo.isCanComment());
        cv.put(PhotosColumns.LIKES, dbo.getLikesCount());
        cv.put(PhotosColumns.COMMENTS, dbo.getCommentsCount());
        cv.put(PhotosColumns.TAGS, dbo.getTagsCount());
        cv.put(PhotosColumns.ACCESS_KEY, dbo.getAccessKey());
        cv.put(PhotosColumns.DELETED, dbo.isDeleted());
        return cv;
    }

    @Override
    public Single<List<PhotoEntity>> findPhotosByCriteriaRx(@NonNull PhotoCriteria criteria) {
        return Single.create(e -> {
            String selection = getSelectionForCriteria(criteria);

            String orderBy = criteria.getOrderBy() == null ? PhotosColumns.PHOTO_ID + " DESC" : criteria.getOrderBy();

            Uri uri = MessengerContentProvider.getPhotosContentUriFor(criteria.getAccountId());
            Cursor cursor = getContext().getContentResolver().query(uri, null, selection, null, orderBy);

            ArrayList<PhotoEntity> photos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if(e.isDisposed()){
                        break;
                    }

                    photos.add(mapPhotoDbo(cursor));
                }

                cursor.close();
            }

            e.onSuccess(photos);
        });
    }

    @Override
    public Completable applyPatch(int accountId, int ownerId, int photoId, PhotoPatch patch) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();

            if(nonNull(patch.getLike())){
                cv.put(PhotosColumns.LIKES, patch.getLike().getCount());
                cv.put(PhotosColumns.USER_LIKES, patch.getLike().isLiked());
            }

            if(nonNull(patch.getDeletion())){
                cv.put(PhotosColumns.DELETED, patch.getDeletion().isDeleted());
            }

            if(cv.size() > 0){
                final Uri uri = MessengerContentProvider.getPhotosContentUriFor(accountId);
                final String where = PhotosColumns.PHOTO_ID + " = ? AND " + PhotosColumns.OWNER_ID + " = ?";
                final String[] args = {String.valueOf(photoId), String.valueOf(ownerId)};

                getContentResolver().update(uri, cv, where, args);
            }
        });
    }

    private static String getSelectionForCriteria(@NonNull PhotoCriteria criteria){
        String selection = "1 = 1";
        if (criteria.getOwnerId() != null) {
            selection = selection + " AND " + PhotosColumns.OWNER_ID + " = " + criteria.getOwnerId();
        }

        if (criteria.getAlbumId() != null) {
            selection = selection + " AND " + PhotosColumns.ALBUM_ID + " = " + criteria.getAlbumId();
        }

        DatabaseIdRange range = criteria.getRange();
        if (nonNull(range)) {
            selection = selection + " AND " + PhotosColumns._ID + " >= " + range.getFirst() +
                    " AND " + PhotosColumns._ID + " <= " + criteria.getRange().getLast();
        }

        return selection;
    }

    private PhotoEntity mapPhotoDbo(Cursor cursor) {
        PhotoSizeEntity sizes = null;

        String sizesJson = cursor.getString(cursor.getColumnIndex(PhotosColumns.SIZES));
        if(nonEmpty(sizesJson)){
            sizes = GSON.fromJson(sizesJson, PhotoSizeEntity.class);
        }

        final int id = cursor.getInt(cursor.getColumnIndex(PhotosColumns.PHOTO_ID));
        final int ownerId = cursor.getInt(cursor.getColumnIndex(PhotosColumns.OWNER_ID));
        return new PhotoEntity(id, ownerId)
                .setSizes(sizes)
                .setAlbumId(cursor.getInt(cursor.getColumnIndex(PhotosColumns.ALBUM_ID)))
                .setWidth(cursor.getInt(cursor.getColumnIndex(PhotosColumns.WIDTH)))
                .setHeight(cursor.getInt(cursor.getColumnIndex(PhotosColumns.HEIGHT)))
                .setText(cursor.getString(cursor.getColumnIndex(PhotosColumns.TEXT)))
                .setDate(cursor.getLong(cursor.getColumnIndex(PhotosColumns.DATE)))
                .setUserLikes(cursor.getInt(cursor.getColumnIndex(PhotosColumns.USER_LIKES)) == 1)
                .setCanComment(cursor.getInt(cursor.getColumnIndex(PhotosColumns.CAN_COMMENT)) == 1)
                .setLikesCount(cursor.getInt(cursor.getColumnIndex(PhotosColumns.LIKES)))
                .setCommentsCount(cursor.getInt(cursor.getColumnIndex(PhotosColumns.COMMENTS)))
                .setTagsCount(cursor.getInt(cursor.getColumnIndex(PhotosColumns.TAGS)))
                .setAccessKey(cursor.getString(cursor.getColumnIndex(PhotosColumns.ACCESS_KEY)))
                .setDeleted(cursor.getInt(cursor.getColumnIndex(PhotosColumns.DELETED)) == 1);
    }
}
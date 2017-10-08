package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.PhotoAlbumsColumns;
import biz.dealnote.messenger.db.interfaces.IPhotoAlbumsStore;
import biz.dealnote.messenger.db.model.entity.PhotoAlbumEntity;
import biz.dealnote.messenger.db.model.entity.PhotoSizeEntity;
import biz.dealnote.messenger.db.model.entity.PrivacyEntity;
import biz.dealnote.messenger.model.criteria.PhotoAlbumsCriteria;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by ruslan.kolbasa on 29.11.2016.
 * phoenix
 */
class PhotoAlbumsStore extends AbsStore implements IPhotoAlbumsStore {

    PhotoAlbumsStore(@NonNull AppStores base) {
        super(base);
    }

    @Override
    public Single<Optional<PhotoAlbumEntity>> findAlbumById(int accountId, int ownerId, int albumId) {
        return Single.create(e -> {
            String where = PhotoAlbumsColumns.OWNER_ID + " = ? AND " + PhotoAlbumsColumns.ALBUM_ID + " = ?";
            String[] args = {String.valueOf(ownerId), String.valueOf(albumId)};
            Uri uri = MessengerContentProvider.getPhotoAlbumsContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            PhotoAlbumEntity album = null;
            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    album = mapAlbum(cursor);
                }

                cursor.close();
            }

            e.onSuccess(Optional.wrap(album));
        });
    }

    @Override
    public Single<List<PhotoAlbumEntity>> findAlbumsByCriteria(@NonNull PhotoAlbumsCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getPhotoAlbumsContentUriFor(criteria.getAccountId());
            Cursor cursor = getContext().getContentResolver().query(uri, null, PhotoAlbumsColumns.OWNER_ID + " = ?",
                    new String[]{String.valueOf(criteria.getOwnerId())}, null);

            List<PhotoAlbumEntity> data = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    data.add(mapAlbum(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Completable store(int accountId, int ownerId, @NonNull List<PhotoAlbumEntity> albums, boolean clearBeforeStore) {
        return Completable.create(e -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(clearBeforeStore ? albums.size() + 1 : albums.size());
            Uri uri = MessengerContentProvider.getPhotoAlbumsContentUriFor(accountId);

            if(clearBeforeStore){
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(PhotoAlbumsColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                        .build());
            }

            for(PhotoAlbumEntity dbo : albums){
                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(createCv(dbo))
                        .build());
            }

            getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    private static ContentValues createCv(PhotoAlbumEntity dbo){
        ContentValues cv = new ContentValues();
        cv.put(PhotoAlbumsColumns.ALBUM_ID, dbo.getId());
        cv.put(PhotoAlbumsColumns.OWNER_ID, dbo.getOwnerId());
        cv.put(PhotoAlbumsColumns.TITLE, dbo.getTitle());
        cv.put(PhotoAlbumsColumns.SIZE, dbo.getSize());
        cv.put(PhotoAlbumsColumns.PRIVACY_VIEW, nonNull(dbo.getPrivacyView()) ? GSON.toJson(dbo.getPrivacyView()) : null);
        cv.put(PhotoAlbumsColumns.PRIVACY_COMMENT, nonNull(dbo.getPrivacyComment()) ? GSON.toJson(dbo.getPrivacyComment()) : null);
        cv.put(PhotoAlbumsColumns.DESCRIPTION, dbo.getDescription());
        cv.put(PhotoAlbumsColumns.CAN_UPLOAD, dbo.isCanUpload());
        cv.put(PhotoAlbumsColumns.UPDATED, dbo.getUpdatedTime());
        cv.put(PhotoAlbumsColumns.CREATED, dbo.getCreatedTime());

        if(Objects.nonNull(dbo.getSizes())){
            cv.put(PhotoAlbumsColumns.SIZES, GSON.toJson(dbo.getSizes()));
        } else {
            cv.putNull(PhotoAlbumsColumns.SIZES);
        }

        cv.put(PhotoAlbumsColumns.UPLOAD_BY_ADMINS, dbo.isUploadByAdminsOnly());
        cv.put(PhotoAlbumsColumns.COMMENTS_DISABLED, dbo.isCommentsDisabled());
        return cv;
    }

    @Override
    public Completable removeAlbumById(int accountId, int ownerId, int albumId) {
        return Completable.create(e -> {
            String where = PhotoAlbumsColumns.OWNER_ID + " = ? AND " + PhotoAlbumsColumns.ALBUM_ID + " = ?";
            String[] args = {String.valueOf(ownerId), String.valueOf(albumId)};
            Uri uri = MessengerContentProvider.getPhotoAlbumsContentUriFor(accountId);

            getContext().getContentResolver().delete(uri, where, args);
            e.onComplete();
        });
    }

    private PhotoAlbumEntity mapAlbum(Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.ALBUM_ID));
        final int ownerId = cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.OWNER_ID));

        PhotoAlbumEntity album = new PhotoAlbumEntity(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndex(PhotoAlbumsColumns.TITLE)))
                .setSize(cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.SIZE)))
                .setDescription(cursor.getString(cursor.getColumnIndex(PhotoAlbumsColumns.DESCRIPTION)))
                .setCanUpload(cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.CAN_UPLOAD)) == 1)
                .setUpdatedTime(cursor.getLong(cursor.getColumnIndex(PhotoAlbumsColumns.UPDATED)))
                .setCreatedTime(cursor.getLong(cursor.getColumnIndex(PhotoAlbumsColumns.CREATED)))
                .setUploadByAdminsOnly(cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.UPLOAD_BY_ADMINS)) == 1)
                .setCommentsDisabled(cursor.getInt(cursor.getColumnIndex(PhotoAlbumsColumns.COMMENTS_DISABLED)) == 1);

        String sizesJson = cursor.getString(cursor.getColumnIndex(PhotoAlbumsColumns.SIZES));
        if(nonEmpty(sizesJson)){
            album.setSizes(GSON.fromJson(sizesJson, PhotoSizeEntity.class));
        }

        String privacyViewText = cursor.getString(cursor.getColumnIndex(PhotoAlbumsColumns.PRIVACY_VIEW));
        if(nonEmpty(privacyViewText)){
            album.setPrivacyView(GSON.fromJson(privacyViewText, PrivacyEntity.class));
        }

        String privacyCommentText = cursor.getString(cursor.getColumnIndex(PhotoAlbumsColumns.PRIVACY_COMMENT));
        if(nonEmpty(privacyCommentText)){
            album.setPrivacyComment(GSON.fromJson(privacyCommentText, PrivacyEntity.class));
        }

        return album;
    }
}
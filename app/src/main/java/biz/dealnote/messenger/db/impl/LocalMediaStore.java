package biz.dealnote.messenger.db.impl;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.interfaces.ILocalMediaStore;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.LocalPhoto;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
class LocalMediaStore extends AbsStore implements ILocalMediaStore {

    LocalMediaStore(@NonNull AppStores mRepositoryContext) {
        super(mRepositoryContext);
    }

    private static final String[] PROJECTION = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

   /* private static final String[] VIDEO_PROJECTION = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.TITLE
    };*/

    /*@Override
    public Single<List<LocalVideo>> getVideos() {
        return Single.create(e -> {
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_PROJECTION, null, null, MediaStore.Video.Media.DATE_ADDED);

            ArrayList<LocalVideo> data = new ArrayList<>(safeCountOf(cursor));
            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    data.add(mapVideo(cursor));
                }
                cursor.close();
            }

            e.onSuccess(data);
        });
    }*/

    /*@Override
    public Bitmap getVideoThumbnail(long videoId) {
        return MediaStore.Video.Thumbnails.getThumbnail(getContext().getContentResolver(),
                videoId, MediaStore.Video.Thumbnails.MINI_KIND, null);
    }

    private static LocalVideo mapVideo(Cursor cursor) {
        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        return new LocalVideo(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)), Uri.parse(data))
                .setDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)))
                .setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)));
    }*/

    @Override
    public Single<List<LocalPhoto>> getPhotos(long albumId) {
        return Single.create(e -> {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, MediaStore.Images.Media.BUCKET_ID + " = ?",
                    new String[]{String.valueOf(albumId)},
                    MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");

            ArrayList<LocalPhoto> result = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) break;

                    long imageId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    result.add(new LocalPhoto()
                            .setImageId(imageId)
                            .setFullImageUri(Uri.parse(data)));
                }

                cursor.close();
            }

            e.onSuccess(result);
        });
    }

    @Override
    public Single<List<LocalImageAlbum>> getImageAlbums() {
        return Single.create(e -> {
            final String album = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME;
            final String albumId = MediaStore.Images.ImageColumns.BUCKET_ID;
            final String data = MediaStore.Images.ImageColumns.DATA;
            final String coverId = MediaStore.Images.ImageColumns._ID;
            String[] projection = new String[]{album, albumId, data, coverId, "COUNT(" + coverId + ")"};

            String selection = "1=1) GROUP BY (" + MediaStore.Images.ImageColumns.BUCKET_ID;

            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, selection, null, MediaStore.Images.ImageColumns.DATE_ADDED + " desc");

            List<LocalImageAlbum> albums = new ArrayList<>(safeCountOf(cursor));

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) break;

                    albums.add(new LocalImageAlbum()
                            .setId(cursor.getInt(1))
                            .setName(cursor.getString(0))
                            .setCoverPath(cursor.getString(2))
                            .setCoverId(cursor.getLong(3))
                            .setPhotoCount(cursor.getInt(4)));
                }

                cursor.close();
            }

            e.onSuccess(albums);
        });
    }

    @Override
    public Bitmap getImageThumbnail(long imageId) {
        return MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),
                imageId, MediaStore.Images.Thumbnails.MINI_KIND, null);
    }
}

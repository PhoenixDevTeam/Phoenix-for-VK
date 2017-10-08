package biz.dealnote.messenger.upload.task;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.WeakPercentageListener;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadCallback;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.ExifGeoDegree;
import biz.dealnote.messenger.util.IOUtils;
import biz.dealnote.messenger.util.Objects;
import retrofit2.Call;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class PhotoToAlbumTask extends AbstractUploadTask<PhotoToAlbumTask.Response> {

    private int albumId;
    private Integer groupId;

    public PhotoToAlbumTask(@NonNull Context cntx, @NonNull UploadCallback callback,
                            @NonNull UploadObject uploadObject, @Nullable UploadServer server) {
        super(cntx, callback, uploadObject, server);
        this.albumId = uploadObject.getDestination().getId();
        this.groupId = uploadObject.getDestination().getOwnerId() < 0
                ? Math.abs(uploadObject.getDestination().getOwnerId()) : null;
    }

    @Override
    protected Response doUpload(@Nullable UploadServer server, @NonNull UploadObject uploadObject) throws CancelException {
        int accountId = uploadObject.getAccountId();
        Response result = new Response();

        InputStream is = null;
        try {
            is = openStream(getContext(), uploadObject.getFileUri(), uploadObject.getSize());

            if (server == null) {
                server = Apis.get()
                        .vkDefault(accountId)
                        .photos()
                        .getUploadServer(albumId, groupId)
                        .blockingGet();

                result.setServer(server);
            }

            assertCancel(this);

            String serverUrl = server.getUrl();

            Call<UploadPhotoToAlbumDto> call = Apis.get()
                    .uploads()
                    .uploadPhotoToAlbum(serverUrl, is, new WeakPercentageListener(this));

            registerCall(call);

            UploadPhotoToAlbumDto entity;

            try {
                entity = call.execute().body();
            } catch (Exception e) {
                result.setError(e);
                return result;
            } finally {
                unregisterCall(call);
            }

            assertCancel(this);

            Double latitude = null;
            Double longitude = null;

            try {
                ExifInterface exif = new ExifInterface(uploadObject.getFileUri().getPath());
                ExifGeoDegree exifGeoDegree = new ExifGeoDegree(exif);

                if (exifGeoDegree.isValid()) {
                    latitude = exifGeoDegree.getLatitude();
                    longitude = exifGeoDegree.getLongitude();
                }
            } catch (Exception ignored) {
            }

            assertCancel(this);

            List<VKApiPhoto> photos = Apis.get()
                    .vkDefault(accountId)
                    .photos()
                    .save(albumId, groupId, entity.server, entity.photosList, entity.hash, latitude, longitude, null)
                    .blockingGet();

            assertCancel(this);

            if (nonEmpty(photos)) {
                VKApiPhoto dto = photos.get(0);


                result.photo = Dto2Model.transform(dto);

                final PhotoEntity photoEntity = Dto2Entity.buildPhotoDbo(dto);
                Stores.getInstance()
                        .photos()
                        .insertPhotosRx(accountId, photoEntity.getOwnerId(), photoEntity.getAlbumId(), Collections.singletonList(photoEntity), false)
                        .blockingAwait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
        } finally {
            IOUtils.closeStreamQuietly(is);
        }

        return result;
    }

    public static class Response extends BaseUploadResponse {

        public Photo photo;

        @Override
        public boolean isSuccess() {
            return Objects.nonNull(photo);
        }
    }
}
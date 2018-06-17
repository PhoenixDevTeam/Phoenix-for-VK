package biz.dealnote.messenger.api.impl;

import android.support.annotation.NonNull;

import java.io.InputStream;

import biz.dealnote.messenger.api.IUploadRetrofitProvider;
import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.interfaces.IUploadApi;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.api.model.upload.UploadOwnerPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToWallDto;
import biz.dealnote.messenger.api.services.IUploadService;
import biz.dealnote.messenger.api.util.ProgressRequestBody;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class UploadApi implements IUploadApi {

    private final IUploadRetrofitProvider provider;

    UploadApi(IUploadRetrofitProvider provider) {
        this.provider = provider;
    }

    private IUploadService service(){
        return provider.provideUploadRetrofit().blockingGet().create(IUploadService.class);
    }

    private static ProgressRequestBody.UploadCallbacks wrapPercentageListener(final PercentagePublisher listener){
        return percentage -> {
            if(Objects.nonNull(listener)){
                listener.onProgressChanged(percentage);
            }
        };
    }

    @Override
    public Single<UploadDocDto> uploadDocumentRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, body);
        return service().uploadDocumentRx(server, part);
    }

    @Override
    public Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadOwnerPhotoRx(server, part);
    }

    @Override
    public Single<UploadPhotoToWallDto> uploadPhotoToWallRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadPhotoToWallRx(server, part);
    }

    @Override
    public Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadPhotoToMessageRx(server, part);
    }

    @Override
    public Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file1", "photo.jpg", body);
        return service().uploadPhotoToAlbumRx(server, part);
    }
}
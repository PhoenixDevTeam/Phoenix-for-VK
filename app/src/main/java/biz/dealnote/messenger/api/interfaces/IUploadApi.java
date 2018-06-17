package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.NonNull;

import java.io.InputStream;

import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.api.model.upload.UploadOwnerPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToWallDto;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IUploadApi {
    Single<UploadDocDto> uploadDocumentRx(String server, String filename, @NonNull InputStream doc, PercentagePublisher listener);

    Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToWallDto> uploadPhotoToWallRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(String server, @NonNull InputStream is, PercentagePublisher listener);

    Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(String server, @NonNull InputStream file1, PercentagePublisher listener);
}
package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.NonNull;

import java.io.InputStream;

import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.api.model.upload.UploadOwnerPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToWallDto;
import retrofit2.Call;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IUploadApi {
    Call<UploadDocDto> uploadDocument(String server, String filename, @NonNull InputStream doc, PercentageListener listener);

    Call<UploadOwnerPhotoDto> uploadOwnerPhoto(String server, @NonNull InputStream photo, PercentageListener listener);

    Call<UploadPhotoToWallDto> uploadPhotoToWall(String server, @NonNull InputStream photo, PercentageListener listener);

    Call<UploadPhotoToMessageDto> uploadPhotoToMessage(String server, @NonNull InputStream photo, PercentageListener listener);

    Call<UploadPhotoToAlbumDto> uploadPhotoToAlbum(String server, @NonNull InputStream file1, PercentageListener listener);
}
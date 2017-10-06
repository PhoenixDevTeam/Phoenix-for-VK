package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.api.model.upload.UploadOwnerPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToWallDto;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

/**
 * Created by ruslan.kolbasa on 26.12.2016.
 * phoenix
 */
public interface IUploadService {

    @Multipart
    @POST
    Call<UploadDocDto> uploadDocument(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Call<UploadOwnerPhotoDto> uploadOwnerPhoto(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Call<UploadPhotoToWallDto> uploadPhotoToWall(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Call<UploadPhotoToMessageDto> uploadPhotoToMessage(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Call<UploadPhotoToAlbumDto> uploadPhotoToAlbum(@Url String server, @Part MultipartBody.Part file1);
}

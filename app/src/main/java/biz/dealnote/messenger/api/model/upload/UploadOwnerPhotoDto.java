package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class UploadOwnerPhotoDto {

    @SerializedName("server")
    public String server;

    @SerializedName("photo")
    public String photo;

    @SerializedName("hash")
    public String hash;

    @Override
    public String toString() {
        return "UploadOwnerPhotoDto{" +
                "server=" + server +
                ", photo='" + photo + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}

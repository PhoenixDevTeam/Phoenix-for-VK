package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class UploadPhotoToAlbumDto {

    @SerializedName("server")
    public int server;

    @SerializedName("photos_list")
    public String photosList;

    @SerializedName("aid")
    public int aid;

    @SerializedName("hash")
    public String hash;

    @Override
    public String toString() {
        return "UploadPhotoToAlbumDto{" +
                "server=" + server +
                ", photosList='" + photosList + '\'' +
                ", aid=" + aid +
                ", hash='" + hash + '\'' +
                '}';
    }
}

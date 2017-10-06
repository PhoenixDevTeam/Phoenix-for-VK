package biz.dealnote.messenger.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiWallUploadServer implements Parcelable, UploadServer {

    /**
     * адрес для загрузки фотографий
     */
    public String upload_url;

    /**
     * идентификатор альбома, в который будет загружена фотография
     */
    public int album_id;

    /**
     * идентификатор пользователя, от чьего имени будет загружено фото
     */
    public int user_id;

    public VkApiWallUploadServer() {

    }

    protected VkApiWallUploadServer(Parcel in) {
        this.upload_url = in.readString();
        this.album_id = in.readInt();
        this.user_id = in.readInt();
    }

    public static final Creator<VkApiWallUploadServer> CREATOR = new Creator<VkApiWallUploadServer>() {
        @Override
        public VkApiWallUploadServer createFromParcel(Parcel in) {
            return new VkApiWallUploadServer(in);
        }

        @Override
        public VkApiWallUploadServer[] newArray(int size) {
            return new VkApiWallUploadServer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(upload_url);
        dest.writeInt(album_id);
        dest.writeInt(user_id);
    }

    @Override
    public String getUrl() {
        return upload_url;
    }
}

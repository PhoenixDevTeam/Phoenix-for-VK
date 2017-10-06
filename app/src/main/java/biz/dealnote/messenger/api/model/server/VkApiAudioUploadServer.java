package biz.dealnote.messenger.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiAudioUploadServer implements Parcelable, UploadServer {

    public String upload_url;

    public VkApiAudioUploadServer() {
    }

    protected VkApiAudioUploadServer(Parcel in) {
        upload_url = in.readString();
    }

    public static final Creator<VkApiAudioUploadServer> CREATOR = new Creator<VkApiAudioUploadServer>() {
        @Override
        public VkApiAudioUploadServer createFromParcel(Parcel in) {
            return new VkApiAudioUploadServer(in);
        }

        @Override
        public VkApiAudioUploadServer[] newArray(int size) {
            return new VkApiAudioUploadServer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(upload_url);
    }

    @Override
    public String getUrl() {
        return upload_url;
    }
}

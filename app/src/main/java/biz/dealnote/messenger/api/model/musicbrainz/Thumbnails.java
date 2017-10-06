package biz.dealnote.messenger.api.model.musicbrainz;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Thumbnails implements Parcelable {

    @SerializedName("large")
    public String large;

    @SerializedName("small")
    public String small;

    protected Thumbnails(Parcel in) {
        this.large = in.readString();
        this.small = in.readString();
    }

    public static final Creator<Thumbnails> CREATOR = new Creator<Thumbnails>() {
        @Override
        public Thumbnails createFromParcel(Parcel in) {
            return new Thumbnails(in);
        }

        @Override
        public Thumbnails[] newArray(int size) {
            return new Thumbnails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(large);
        dest.writeString(small);
    }
}
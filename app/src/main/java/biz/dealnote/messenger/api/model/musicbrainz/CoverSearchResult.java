package biz.dealnote.messenger.api.model.musicbrainz;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoverSearchResult implements Parcelable {

    @SerializedName("images")
    public List<Image> images;

    @SerializedName("release")
    public String release;

    protected CoverSearchResult(Parcel in) {
        this.images = in.createTypedArrayList(Image.CREATOR);
        this.release = in.readString();
    }

    public static final Creator<CoverSearchResult> CREATOR = new Creator<CoverSearchResult>() {
        @Override
        public CoverSearchResult createFromParcel(Parcel in) {
            return new CoverSearchResult(in);
        }

        @Override
        public CoverSearchResult[] newArray(int size) {
            return new CoverSearchResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(images);
        dest.writeString(release);
    }
}
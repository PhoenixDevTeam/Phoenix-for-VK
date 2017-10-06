package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Cover implements Parcelable {

    public int audioId;
    public int ownerId;
    public String large;
    public String small;

    public Cover(){}

    protected Cover(Parcel in) {
        audioId = in.readInt();
        ownerId = in.readInt();
        large = in.readString();
        small = in.readString();
    }

    public static final Creator<Cover> CREATOR = new Creator<Cover>() {
        @Override
        public Cover createFromParcel(Parcel in) {
            return new Cover(in);
        }

        @Override
        public Cover[] newArray(int size) {
            return new Cover[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(audioId);
        dest.writeInt(ownerId);
        dest.writeString(large);
        dest.writeString(small);
    }
}

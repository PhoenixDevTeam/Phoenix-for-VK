package biz.dealnote.messenger.model.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 04.10.2017.
 * phoenix
 */
public final class University implements Parcelable {

    private final int id;

    private final String title;

    public University(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private University(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    public static final Creator<University> CREATOR = new Creator<University>() {
        @Override
        public University createFromParcel(Parcel in) {
            return new University(in);
        }

        @Override
        public University[] newArray(int size) {
            return new University[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
    }
}
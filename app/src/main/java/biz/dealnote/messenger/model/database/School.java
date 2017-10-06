package biz.dealnote.messenger.model.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 04.10.2017.
 * phoenix
 */
public final class School implements Parcelable {

    private final int id;

    private final String title;

    public School(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private School(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    public static final Creator<School> CREATOR = new Creator<School>() {
        @Override
        public School createFromParcel(Parcel in) {
            return new School(in);
        }

        @Override
        public School[] newArray(int size) {
            return new School[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
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
package biz.dealnote.messenger.model.database;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 04.10.2017.
 * phoenix
 */
public final class Faculty implements Parcelable {

    private final int id;

    private final String title;

    public Faculty(int id, String title) {
        this.id = id;
        this.title = title;
    }

    private Faculty(Parcel in) {
        id = in.readInt();
        title = in.readString();
    }

    public static final Creator<Faculty> CREATOR = new Creator<Faculty>() {
        @Override
        public Faculty createFromParcel(Parcel in) {
            return new Faculty(in);
        }

        @Override
        public Faculty[] newArray(int size) {
            return new Faculty[size];
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
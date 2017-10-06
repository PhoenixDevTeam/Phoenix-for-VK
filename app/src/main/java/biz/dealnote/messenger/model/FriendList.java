package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public final class FriendList implements Parcelable {

    private final int id;

    private final String name;

    public FriendList(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private FriendList(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public static final Creator<FriendList> CREATOR = new Creator<FriendList>() {
        @Override
        public FriendList createFromParcel(Parcel in) {
            return new FriendList(in);
        }

        @Override
        public FriendList[] newArray(int size) {
            return new FriendList[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
    }
}

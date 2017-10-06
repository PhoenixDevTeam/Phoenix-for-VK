package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 18.06.2017.
 * phoenix
 */
public final class SelectProfileCriteria implements Parcelable {

    private boolean friendsOnly;

    public SelectProfileCriteria(Parcel in) {
        friendsOnly = in.readByte() != 0;
    }

    public static final Creator<SelectProfileCriteria> CREATOR = new Creator<SelectProfileCriteria>() {
        @Override
        public SelectProfileCriteria createFromParcel(Parcel in) {
            return new SelectProfileCriteria(in);
        }

        @Override
        public SelectProfileCriteria[] newArray(int size) {
            return new SelectProfileCriteria[size];
        }
    };

    public SelectProfileCriteria() {

    }

    public SelectProfileCriteria setFriendsOnly(boolean friendsOnly) {
        this.friendsOnly = friendsOnly;
        return this;
    }

    public boolean isFriendsOnly() {
        return friendsOnly;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (friendsOnly ? 1 : 0));
    }
}

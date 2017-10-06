package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 22.11.2016.
 * phoenix
 */
public class UserOnline extends AbsRealtimeVkAction implements Parcelable {

    private final int userId;

    public UserOnline(int accountId, int userId) {
        super(accountId, RealtimeAction.USER_IS_ONLINE);
        this.userId = userId;
    }

    protected UserOnline(Parcel in) {
        super(in);
        userId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(userId);
    }

    public static final Creator<UserOnline> CREATOR = new Creator<UserOnline>() {
        @Override
        public UserOnline createFromParcel(Parcel in) {
            return new UserOnline(in);
        }

        @Override
        public UserOnline[] newArray(int size) {
            return new UserOnline[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "UserOnline{" +
                "userId=" + userId +
                '}';
    }
}

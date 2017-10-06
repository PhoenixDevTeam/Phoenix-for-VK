package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 22.11.2016.
 * phoenix
 */
public class UserOffline extends AbsRealtimeVkAction implements Parcelable {

    private final int userId;

    private final boolean byTimeout;

    public UserOffline(int accountId, int userId, boolean byTimeout) {
        super(accountId, RealtimeAction.USER_IS_OFFLINE);
        this.userId = userId;
        this.byTimeout = byTimeout;
    }

    protected UserOffline(Parcel in) {
        super(in);
        userId = in.readInt();
        byTimeout = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(userId);
        dest.writeByte((byte) (byTimeout ? 1 : 0));
    }

    public static final Creator<UserOffline> CREATOR = new Creator<UserOffline>() {
        @Override
        public UserOffline createFromParcel(Parcel in) {
            return new UserOffline(in);
        }

        @Override
        public UserOffline[] newArray(int size) {
            return new UserOffline[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isByTimeout() {
        return byTimeout;
    }

    @Override
    public String toString() {
        return "UserOffline{" +
                "userId=" + userId +
                ", byTimeout=" + byTimeout +
                '}';
    }
}

package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 22.11.2016.
 * phoenix
 */
public class WriteText extends AbsRealtimeVkAction implements Parcelable {

    private final int userId;

    private final int peerId;

    public WriteText(int accountId, int userId, int peerId) {
        super(accountId, RealtimeAction.USER_WRITE_TEXT);
        this.userId = userId;
        this.peerId = peerId;
    }

    protected WriteText(Parcel in) {
        super(in);
        userId = in.readInt();
        peerId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(userId);
        dest.writeInt(peerId);
    }

    public static final Creator<WriteText> CREATOR = new Creator<WriteText>() {
        @Override
        public WriteText createFromParcel(Parcel in) {
            return new WriteText(in);
        }

        @Override
        public WriteText[] newArray(int size) {
            return new WriteText[size];
        }
    };

    public int getPeerId() {
        return peerId;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

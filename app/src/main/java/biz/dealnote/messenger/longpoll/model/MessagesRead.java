package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public final class MessagesRead extends AbsRealtimeVkAction implements Parcelable {

    private final int peerId;

    private final int toMessageId;

    private final boolean out;

    private final int unreadCount;

    public MessagesRead(int accountId, int peerId, int toMessageId, boolean out, int unreadCount) {
        super(accountId, RealtimeAction.MESSAGES_READ);
        this.peerId = peerId;
        this.toMessageId = toMessageId;
        this.out = out;
        this.unreadCount = unreadCount;
    }

    private MessagesRead(Parcel in) {
        super(in);
        peerId = in.readInt();
        toMessageId = in.readInt();
        out = in.readByte() != 0;
        unreadCount = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(peerId);
        dest.writeInt(toMessageId);
        dest.writeByte((byte) (out ? 1 : 0));
        dest.writeInt(unreadCount);
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public static final Creator<MessagesRead> CREATOR = new Creator<MessagesRead>() {
        @Override
        public MessagesRead createFromParcel(Parcel in) {
            return new MessagesRead(in);
        }

        @Override
        public MessagesRead[] newArray(int size) {
            return new MessagesRead[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getPeerId() {
        return peerId;
    }

    public boolean isOut() {
        return out;
    }

    public int getToMessageId() {
        return toMessageId;
    }
}
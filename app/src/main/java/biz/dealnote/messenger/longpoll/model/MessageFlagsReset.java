package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public class MessageFlagsReset extends AbsRealtimeVkAction implements Parcelable {

    private final int messageId;

    private final int mask;
    private final int peerId;

    public MessageFlagsReset(int accountId, int messageId, int peerId, int mask) {
        super(accountId, RealtimeAction.MESSAGES_FLAGS_RESET);
        this.messageId = messageId;
        this.mask = mask;
        this.peerId = peerId;
    }

    public int getMask() {
        return mask;
    }

    public int getMessageId() {
        return messageId;
    }

    protected MessageFlagsReset(Parcel in) {
        super(in);
        messageId = in.readInt();
        mask = in.readInt();
        peerId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(messageId);
        dest.writeInt(mask);
        dest.writeInt(peerId);
    }

    public static final Creator<MessageFlagsReset> CREATOR = new Creator<MessageFlagsReset>() {
        @Override
        public MessageFlagsReset createFromParcel(Parcel in) {
            return new MessageFlagsReset(in);
        }

        @Override
        public MessageFlagsReset[] newArray(int size) {
            return new MessageFlagsReset[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public int getPeerId() {
        return peerId;
    }
}

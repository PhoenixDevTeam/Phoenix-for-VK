package biz.dealnote.messenger.longpoll.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 23.11.2016.
 * phoenix
 */
public class MessageFlagsSet extends AbsRealtimeVkAction implements Parcelable {

    private final int messageId;

    private final int mask;

    private final int peerId;

    public MessageFlagsSet(int accountId, int messageId, int peerId, int mask) {
        super(accountId, RealtimeAction.MESSAGES_FLAGS_SET);
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

    protected MessageFlagsSet(Parcel in) {
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

    public int getPeerId() {
        return peerId;
    }

    public static final Creator<MessageFlagsSet> CREATOR = new Creator<MessageFlagsSet>() {
        @Override
        public MessageFlagsSet createFromParcel(Parcel in) {
            return new MessageFlagsSet(in);
        }

        @Override
        public MessageFlagsSet[] newArray(int size) {
            return new MessageFlagsSet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}

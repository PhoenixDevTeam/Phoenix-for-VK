package biz.dealnote.messenger.api.model.longpoll;

public class MessageFlagsResetUpdate extends AbsLongpollEvent {

    public MessageFlagsResetUpdate() {
        super(ACTION_MESSAGES_FLAGS_RESET);
    }

    public int message_id;
    public int mask;
    public int peer_id;

    public int getMessageId() {
        return message_id;
    }

    public int getMask() {
        return mask;
    }

    public int getPeerId() {
        return peer_id;
    }
}
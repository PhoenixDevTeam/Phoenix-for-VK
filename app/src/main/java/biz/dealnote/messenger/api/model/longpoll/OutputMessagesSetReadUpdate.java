package biz.dealnote.messenger.api.model.longpoll;

public class OutputMessagesSetReadUpdate extends AbsLongpollEvent {

    public OutputMessagesSetReadUpdate() {
        super(ACTION_SET_OUTPUT_MESSAGES_AS_READ);
    }

    public int peer_id;
    public int local_id;

    public int getPeerId() {
        return peer_id;
    }

    public int getLocalId() {
        return local_id;
    }
}

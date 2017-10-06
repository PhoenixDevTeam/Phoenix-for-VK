package biz.dealnote.messenger.api.model.longpoll;

import android.text.TextUtils;

import java.util.ArrayList;

import biz.dealnote.messenger.api.model.VKApiMessage;

public class AddMessageUpdate extends AbsLongpollEvent {

    public AddMessageUpdate() {
        super(ACTION_MESSAGE_ADDED);
    }

    public int message_id;
    public int flags;
    public long timestamp;
    public String subject;
    public String text;
    public int from;

    public String sourceText;
    public String sourceAct;
    public int sourceMid;
    public ArrayList<String> fwds;
    public int peer_id;
    public String random_id;

    public int getMessageId() {
        return message_id;
    }

    public boolean isOut(){
        return (flags & VKApiMessage.FLAG_OUTBOX) != 0;
    }

    public String getText() {
        return text;
    }

    public boolean isGroupChat() {
        return (flags & VKApiMessage.FLAG_GROUP_CHAT) != 0;
    }

    public boolean hasMedia() {
        return (flags & VKApiMessage.FLAG_MEDIA) != 0;
    }

    public boolean hasFwds() {
        return fwds != null && fwds.size() > 0;
    }

    public boolean isServiceMessage() {
        return !TextUtils.isEmpty(sourceAct);
    }

    public boolean isFull() {
        return !hasMedia() && !hasFwds() && !isServiceMessage();
    }

    public int getPeerId() {
        return peer_id;
    }
}
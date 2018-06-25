package biz.dealnote.messenger.api.model.longpoll;

import android.text.TextUtils;

import java.util.ArrayList;

import biz.dealnote.messenger.model.Peer;

public class AddMessageUpdate extends AbsLongpollEvent {

    public AddMessageUpdate() {
        super(ACTION_MESSAGE_ADDED);
    }

    public int message_id;

    public long timestamp;
    public String subject;
    public String text;
    public int from;
    public boolean outbox;
    public boolean unread;
    public boolean important;
    public boolean deleted;
    public boolean hasMedia;
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
        return outbox;
    }

    public String getText() {
        return text;
    }

    public boolean isGroupChat() {
        return Peer.isGroupChat(peer_id);
    }

    public boolean hasMedia() {
        return hasMedia;
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
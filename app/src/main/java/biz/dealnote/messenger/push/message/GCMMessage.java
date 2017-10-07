package biz.dealnote.messenger.push.message;

import android.os.Bundle;

import biz.dealnote.messenger.api.util.VKStringUtils;
import biz.dealnote.messenger.push.NotificationUtils;

public class GCMMessage {

    public int peer_id;
    public String text;
    public String collapse_key;
    public int message_id;
    private int badge;
    public String from;
    public String type;
    public String _genSrv;
    public long google_sent_time;

    public static GCMMessage genFromBundle(Bundle bundle) {
        GCMMessage message = new GCMMessage();
        message.collapse_key = bundle.getString("collapse_key");
        message.peer_id = Integer.parseInt(bundle.getString("uid"));
        message.text = VKStringUtils.unescape(bundle.getString("text"));
        message.message_id = Integer.parseInt(bundle.getString("msg_id"));
        message.badge = NotificationUtils.optInt(bundle, "badge", -1);
        message.from = bundle.getString("from");
        message.type = bundle.getString("type");
        message._genSrv = bundle.getString("_genSrv");
        message.google_sent_time = bundle.getLong("google.sent_time", 0L);
        return message;
    }

    public int getBadge() {
        return badge;
    }

    public int getPeerId() {
        return peer_id;
    }

    public int getMessageId() {
        return message_id;
    }

    public String getText() {
        return text;
    }

    public long getGoogleSentTime() {
        return google_sent_time;
    }
}
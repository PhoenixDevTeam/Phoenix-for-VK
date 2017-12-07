package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VkApiDialog;

public final class DialogsColumns implements BaseColumns {

    private DialogsColumns(){}

    public static final String TABLENAME = "dialogs";

    public static final String UNREAD = "unread";
    public static final String TITLE = "title";

    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";

    public static final String ADMIN_ID = "admin_id";

    public static final String LAST_MESSAGE_ID = "last_message_id";

    public static ContentValues getCV(VkApiDialog d){
        ContentValues cv = new ContentValues();
        cv.put(_ID, d.message.peer_id);
        cv.put(UNREAD, d.unread);
        cv.put(TITLE, d.message.title);
        cv.put(PHOTO_50, d.message.photo_50);
        cv.put(PHOTO_100, d.message.photo_100);
        cv.put(PHOTO_200, d.message.photo_200);
        cv.put(ADMIN_ID, d.message.admin_id);
        cv.put(LAST_MESSAGE_ID, d.message.id);
        return cv;
    }

    public static ContentValues getCV(int peerId, String title, int unreadCount){
        ContentValues cv = new ContentValues();
        cv.put(_ID, peerId);
        cv.put(UNREAD, unreadCount);
        cv.put(TITLE, title);
        return cv;
    }

    public static ContentValues getCV(VKApiChat chat){
        ContentValues cv = new ContentValues();
        cv.put(_ID, VKApiMessage.CHAT_PEER + chat.id);
        cv.put(TITLE, chat.title);
        cv.put(PHOTO_200, chat.photo_200);
        cv.put(PHOTO_100, chat.photo_100);
        cv.put(PHOTO_50, chat.photo_50);
        cv.put(ADMIN_ID, chat.admin_id);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_UNREAD = TABLENAME + "." + UNREAD;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_ADMIN_ID = TABLENAME + "." + ADMIN_ID;
    public static final String FULL_LAST_MESSAGE_ID = TABLENAME + "." + LAST_MESSAGE_ID;

    public static final String FOREIGN_MESSAGE_FROM_ID = "message_from_id";
    public static final String FOREIGN_MESSAGE_BODY = "message_body";
    public static final String FOREIGN_MESSAGE_DATE = "message_date";
    public static final String FOREIGN_MESSAGE_OUT = "message_out";
    public static final String FOREIGN_MESSAGE_TITLE = "message_title";
    public static final String FOREIGN_MESSAGE_READ_STATE = "message_read_state";
    public static final String FOREIGN_MESSAGE_HAS_ATTACHMENTS = "message_has_attachments";
    public static final String FOREIGN_MESSAGE_FWD_COUNT = "message_forward_count";
    public static final String FOREIGN_MESSAGE_ACTION = "message_action";
    public static final String FOREIGN_MESSAGE_ENCRYPTED = "message_encrypted";
}
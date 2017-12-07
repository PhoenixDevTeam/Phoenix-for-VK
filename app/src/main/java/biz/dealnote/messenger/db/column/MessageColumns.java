package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public final class MessageColumns implements BaseColumns {

    private MessageColumns () {}

    public static final String TABLENAME = "messages";

    public static final String _ID = "_id";
    public static final String PEER_ID = "peer_id";
    public static final String FROM_ID = "from_id";
    public static final String DATE = "date";
    public static final String READ_STATE = "read_state";
    public static final String OUT = "out";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    public static final String ENCRYPTED = "encrypted";
    public static final String IMPORTANT = "important";
    public static final String DELETED = "deleted";
    public static final String FORWARD_COUNT = "fwd_count";
    public static final String HAS_ATTACHMENTS = "has_attachments";
    public static final String STATUS = "status";
    public static final String ATTACH_TO = "attach_to";
    public static final String ORIGINAL_ID = "original_id";

    //chat_columns
    public static final String CHAT_ACTIVE = "chat_active";
    //public static final String PUSH_SETTINGS = "push_settings";
    public static final String USER_COUNT = "users_count";
    public static final String ADMIN_ID = "admin_id";
    public static final String ACTION = "action";
    public static final String ACTION_MID = "action_mid";
    public static final String ACTION_EMAIL = "action_email";
    public static final String ACTION_TEXT = "action_text";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String RANDOM_ID = "random_id";
    public static final String EXTRAS = "extras";

    //public static final String FOREIGN_SENDER_FIRST_NAME = "sender_first_name";
    //public static final String FOREING_SENDER_LAST_NAME = "sender_last_name";
    //public static final String FOREIGN_SENDER_PHOTO_50 = "sender_photo_50";
    //public static final String FOREIGN_SENDER_PHOTO_100 = "sender_photo_100";
    //public static final String FOREIGN_SENDER_PHOTO_200 = "sender_photo_200";

    //public static final String FOREIGN_ACTION_FIRST_NAME = "action_first_name";
    //public static final String FOREIGN_ACTION_LAST_NAME = "action_last_name";

    public static final int DONT_ATTACH = 0;

    /*public static ContentValues getCV(@NonNull VKApiMessage message, int status, int attachTo){
        ContentValues cv = new ContentValues();
        if(attachTo == DONT_ATTACH){ // если сообщение прикреплено к другому сообщению, то ВК не передает его _ID.
            cv.put(_ID, message.id); // Поэтому он будет сгенерирован
        }

        cv.put(PEER_ID, message.peer_id);
        cv.put(FROM_ID, message.from_id);
        cv.put(DATE, message.date);
        cv.put(READ_STATE, message.read_state);
        cv.put(OUT, message.out);
        cv.put(TITLE, message.title);
        cv.put(BODY, message.body);
        cv.put(ENCRYPTED, CryptHelper.analizeMessageBody(message.body) == MessageType.CRYPTED);
        cv.put(DELETED, message.deleted);
        cv.put(IMPORTANT, message.important);
        cv.put(FORWARD_COUNT, Utils.safeCountOf(message.fwd_messages));
        cv.put(HAS_ATTACHMENTS, Objects.isNull(message.attachments) ? 0 : message.attachments.size());
        cv.put(STATUS, status);
        cv.put(ATTACH_TO, attachTo);
        cv.put(ORIGINAL_ID, message.id);

        cv.put(CHAT_ACTIVE, message.chat_active);
        cv.put(PUSH_SETTINGS, message.push_settings);
        cv.put(USER_COUNT, message.users_count);
        cv.put(ADMIN_ID, message.admin_id);
        cv.put(ACTION, Message.fromApiChatAction(message.action));
        cv.put(ACTION_MID, message.action_mid);
        cv.put(ACTION_EMAIL, message.action_email);
        cv.put(ACTION_TEXT, message.action_text);
        cv.put(PHOTO_50, message.photo_50);
        cv.put(PHOTO_100, message.photo_100);
        cv.put(PHOTO_200, message.photo_200);

        int randomId = 0;
        try {
            randomId = Integer.parseInt(message.random_id);
        } catch (NumberFormatException ignored){}

        cv.put(RANDOM_ID, randomId);
        return cv;
    }*/

    /*public static ContentValues getCV(Message item){
        boolean encrypted = item.getCryptStatus() == CryptStatus.ENCRYPTED
                || item.getCryptStatus() == CryptStatus.DECRYPT_FAILED;

        ContentValues cv = new ContentValues();
        if(item.getAttachTo() == DONT_ATTACH){
            cv.put(_ID, item.getId());
        }

        cv.put(PEER_ID, item.getPeerId());
        cv.put(FROM_ID, item.getSenderId());
        cv.put(DATE, item.getDate());
        cv.put(READ_STATE, item.isRead());
        cv.put(OUT, item.isOut());
        cv.put(TITLE, item.getTitle());
        cv.put(BODY, item.getBody());
        cv.put(ENCRYPTED, encrypted);
        cv.put(DELETED, item.isDeleted());
        cv.put(IMPORTANT, item.isImportant());
        if(item.getFwd() != null){
            cv.put(FORWARD_COUNT, item.getFwd().size());
        }

        cv.put(HAS_ATTACHMENTS, item.getAttachments() == null ? 0 : item.getAttachments().size());
        cv.put(STATUS, item.getStatus());
        cv.put(ATTACH_TO, item.getAttachTo());
        cv.put(ORIGINAL_ID, item.getOriginalId());
        cv.put(RANDOM_ID, item.getRandomId());
        return cv;
    }*/

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PEER_ID = TABLENAME + "." + PEER_ID;
    public static final String FULL_FROM_ID = TABLENAME + "." + FROM_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_READ_STATE = TABLENAME + "." + READ_STATE;
    public static final String FULL_OUT = TABLENAME + "." + OUT;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_BODY = TABLENAME + "." + BODY;
    public static final String FULL_ENCRYPTED = TABLENAME + "." + ENCRYPTED;
    public static final String FULL_DELETED = TABLENAME + "." + DELETED;
    public static final String FULL_IMPORTANT = TABLENAME + "." + IMPORTANT;
    public static final String FULL_FORWARD_COUNT = TABLENAME + "." + FORWARD_COUNT;
    public static final String FULL_HAS_ATTACHMENTS = TABLENAME + "." + HAS_ATTACHMENTS;
    public static final String FULL_STATUS = TABLENAME + "." + STATUS;
    public static final String FULL_ATTACH_TO = TABLENAME + "." + ATTACH_TO;
    public static final String FULL_ORIGINAL_ID = TABLENAME + "." + ORIGINAL_ID;

    public static final String FULL_CHAT_ACTIVE = TABLENAME + "." + CHAT_ACTIVE;
    //public static final String FULL_PUSH_SETTINGS = TABLENAME + "." + PUSH_SETTINGS;
    public static final String FULL_USER_COUNT = TABLENAME + "." + USER_COUNT;
    public static final String FULL_ADMIN_ID = TABLENAME + "." + ADMIN_ID;
    public static final String FULL_ACTION = TABLENAME + "." + ACTION;
    public static final String FULL_ACTION_MID = TABLENAME + "." + ACTION_MID;
    public static final String FULL_ACTION_EMAIL = TABLENAME + "." + ACTION_EMAIL;
    public static final String FULL_ACTION_TEXT = TABLENAME + "." + ACTION_TEXT;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_RANDOM_ID = TABLENAME + "." + RANDOM_ID;
    public static final String FULL_EXTRAS = TABLENAME + "." + EXTRAS;
}
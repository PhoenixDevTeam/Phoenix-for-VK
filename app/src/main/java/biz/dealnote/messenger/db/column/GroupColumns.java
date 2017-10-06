package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiCommunity;

public final class GroupColumns implements BaseColumns {

    private GroupColumns() {}

    public static final String TABLENAME = "groups";

    public static final String NAME = "name";
    public static final String SCREEN_NAME = "screen_name";
    public static final String IS_CLOSED = "is_closed";
    public static final String IS_ADMIN = "is_admin";
    public static final String ADMIN_LEVEL = "admin_level";
    public static final String IS_MEMBER = "is_member";
    public static final String MEMBER_STATUS = "member_status";
    public static final String TYPE = "type";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";

    public static final String CAN_ADD_TOPICS = "can_add_topics";
    public static final String TOPICS_ORDER = "topics_order";

    public static final String API_FIELDS = "name, screen_name, is_closed, is_admin, admin_level, " +
            "is_member, member_status, type, photo_50, photo_100, photo_200";

    public static ContentValues getCV(VKApiCommunity u) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, u.id);
        cv.put(NAME, u.name);
        cv.put(SCREEN_NAME, u.screen_name);
        cv.put(IS_CLOSED, u.is_closed);
        cv.put(IS_ADMIN, u.is_admin);
        cv.put(ADMIN_LEVEL, u.admin_level);
        cv.put(IS_MEMBER, u.is_member);
        cv.put(MEMBER_STATUS, u.member_status);
        cv.put(TYPE, u.type);
        cv.put(PHOTO_50, u.photo_50);
        cv.put(PHOTO_100, u.photo_100);
        cv.put(PHOTO_200, u.photo_200);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_NAME = TABLENAME + "." + NAME;
    public static final String FULL_SCREEN_NAME = TABLENAME + "." + SCREEN_NAME;
    public static final String FULL_IS_CLOSED = TABLENAME + "." + IS_CLOSED;
    public static final String FULL_IS_ADMIN = TABLENAME + "." + IS_ADMIN;
    public static final String FULL_ADMIN_LEVEL = TABLENAME + "." + ADMIN_LEVEL;
    public static final String FULL_IS_MEMBER = TABLENAME + "." + IS_MEMBER;
    public static final String FULL_MEMBER_STATUS = TABLENAME + "." + MEMBER_STATUS;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_CAN_ADD_TOPICS = TABLENAME + "." + CAN_ADD_TOPICS;
    public static final String FULL_TOPICS_ORDER = TABLENAME + "." + TOPICS_ORDER;
}
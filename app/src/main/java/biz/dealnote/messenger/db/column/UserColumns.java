package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiUser;

public final class UserColumns implements BaseColumns {

    public static final String API_FIELDS = "first_name, last_name, online, online_mobile, photo_50, " +
            "photo_100, photo_200, last_seen, platform, status, photo_max_orig, online_app, sex, domain, is_friend, friend_status";

    // This class cannot be instantiated
    private UserColumns() {}

    /**
     * The table name of books = "books"
     */
    public static final String TABLENAME = "users";

    //Columns
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String ONLINE = "online";
    public static final String ONLINE_MOBILE = "online_mobile";
    public static final String ONLINE_APP = "online_app";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String LAST_SEEN = "last_seen";
    public static final String PLATFORM = "platform";
    public static final String USER_STATUS = "user_status";
    public static final String SEX = "sex";
    public static final String DOMAIN = "domain";
    public static final String IS_FRIEND = "is_friend";
    public static final String FRIEND_STATUS = "friend_status";

    public static ContentValues getCV(VKApiUser u) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, u.id);
        cv.put(FIRST_NAME, u.first_name);
        cv.put(LAST_NAME, u.last_name);
        cv.put(ONLINE, u.online);
        cv.put(ONLINE_MOBILE, u.online_mobile);
        cv.put(ONLINE_APP, u.online_app);
        cv.put(PHOTO_50, u.photo_50);
        cv.put(PHOTO_100, u.photo_100);
        cv.put(PHOTO_200, u.photo_200);
        cv.put(LAST_SEEN, u.last_seen);
        cv.put(PLATFORM, u.platform);
        cv.put(USER_STATUS, u.status);
        cv.put(SEX, u.sex);
        cv.put(DOMAIN, u.domain);
        cv.put(IS_FRIEND, u.is_friend);
        return cv;
    }

    /**
     * The id of the user, includes tablename prefix
     * <P>Type: INT</P>
     */
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_FIRST_NAME = TABLENAME + "." + FIRST_NAME;
    public static final String FULL_LAST_NAME = TABLENAME + "." + LAST_NAME;
    public static final String FULL_ONLINE = TABLENAME + "." + ONLINE;
    public static final String FULL_ONLINE_MOBILE = TABLENAME + "." + ONLINE_MOBILE;
    public static final String FULL_ONLINE_APP = TABLENAME + "." + ONLINE_APP;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_LAST_SEEN = TABLENAME + "." + LAST_SEEN;
    public static final String FULL_PLATFORM = TABLENAME + "." + PLATFORM;
    public static final String FULL_USER_STATUS = TABLENAME + "." + USER_STATUS;
    public static final String FULL_SEX = TABLENAME + "." + SEX;
    public static final String FULL_DOMAIN = TABLENAME + "." + DOMAIN;
    public static final String FULL_IS_FRIEND = TABLENAME + "." + IS_FRIEND;
    public static final String FULL_FRIEND_STATUS = TABLENAME + "." + FRIEND_STATUS;
}
package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiCommunity;

public final class GroupContactsColumns implements BaseColumns {

    private GroupContactsColumns() {}

    public static final String TABLENAME = "group_contacts";

    public static final String GROUP_ID = "group_id";
    public static final String USER_ID = "user_id";

    public static ContentValues getCV(int gid, VKApiCommunity.Contact u){
        ContentValues cv = new ContentValues();
        cv.put(GROUP_ID, gid);
        cv.put(USER_ID, u.user_id);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_GROUP_ID = TABLENAME + "." + GROUP_ID;
    public static final String FULL_USER_ID = TABLENAME + "." + USER_ID;
}
package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiCommunity;

public final class GroupLinksColumns implements BaseColumns {

    private GroupLinksColumns() {}

    public static final String TABLENAME = "group_links";

    public static final String GROUP_ID = "group_id";
    public static final String LINK_ID = "link_id";
    public static final String NAME = "name";
    public static final String DESC = "desc";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";

    public static ContentValues getCV(int gid, VKApiCommunity.Link u){
        ContentValues cv = new ContentValues();
        cv.put(GROUP_ID, gid);
        cv.put(LINK_ID, u.id);
        cv.put(NAME, u.name);
        cv.put(DESC, u.desc);
        cv.put(PHOTO_50, u.photo_50);
        cv.put(PHOTO_100, u.photo_100);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_GROUP_ID = TABLENAME + "." + GROUP_ID;
    public static final String FULL_LINK_ID = TABLENAME + "." + LINK_ID;
    public static final String FULL_NAME = TABLENAME + "." + NAME;
    public static final String FULL_DESC = TABLENAME + "." + DESC;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
}
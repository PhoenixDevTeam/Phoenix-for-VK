package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

public final class FaveGroupsColumns implements BaseColumns {

    public static final String TABLENAME = "fave_groups";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String NAME = "name";
    public static final String SCREEN_NAME = "screen_name";
    public static final String IS_CLOSED = "is_closed";
    public static final String DESCRIPTION = "description";
    public static final String UPDATED_TIME = "updated_time";
    public static final String FAVE_TYPE = "fave_type";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";

    private FaveGroupsColumns() {
    }

    public static ContentValues buildCV(int id) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, id);
        return cv;
    }

}
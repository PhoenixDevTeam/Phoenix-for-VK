package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VkApiFriendList;

public class FriendListsColumns implements BaseColumns {

    private FriendListsColumns() {}

    public static final String TABLENAME = "friend_lists";

    public static final String USER_ID = "user_id";
    public static final String LIST_ID = "list_id";
    public static final String NAME = "name";

    public static ContentValues getCv(int userId, VkApiFriendList list){
        ContentValues cv = new ContentValues();
        cv.put(USER_ID, userId);
        cv.put(LIST_ID, list.id);
        cv.put(NAME, list.name);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_USER_ID = TABLENAME + "." + USER_ID;
    public static final String FULL_LIST_ID = TABLENAME + "." + LIST_ID;
    public static final String FULL_NAME = TABLENAME + "." + NAME;
}

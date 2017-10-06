package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public class UsersDetColumns implements BaseColumns {

    private UsersDetColumns() {
    }

    public static final String TABLENAME = "users_det";

    public static final String DATA = "data";

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_DATA = TABLENAME + "." + DATA;
}
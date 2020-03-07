package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public final class FavePageColumns implements BaseColumns {
    public static final String TABLENAME = "fave_pages";

    public static final String FULL_ID = TABLENAME + "." + _ID;

    public static final String DESCRIPTION = "description";
    public static final String UPDATED_TIME = "updated_time";
    public static final String FAVE_TYPE = "fave_type";

    private FavePageColumns() {
    }


}
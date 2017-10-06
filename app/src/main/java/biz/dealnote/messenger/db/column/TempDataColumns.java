package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public class TempDataColumns implements BaseColumns {

    public static final String TABLENAME = "temp_app_data";

    public static final String OWNER_ID = "owner_id";
    public static final String SOURCE_ID = "source_id";
    public static final String DATA = "data";
}
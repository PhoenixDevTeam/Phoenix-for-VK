package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogColumns implements BaseColumns {

    public static final String TABLENAME = "logs";

    public static final String TYPE = "eventtype";

    public static final String DATE = "eventdate";

    public static final String TAG = "tag";

    public static final String BODY = "body";
}

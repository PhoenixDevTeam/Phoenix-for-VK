package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public class CoversColumns implements BaseColumns {

    private CoversColumns(){}

    public static final String TABLENAME = "covers";

    public static final String AUDIO_ID = "audio_id";
    public static final String OWNER_ID = "owner_id";
    public static final String DATA = "data";

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_AUDIO_ID = TABLENAME + "." + AUDIO_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_DATA = TABLENAME + "." + DATA;
}

package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

/**
 * Created by ruslan.kolbasa on 27.01.2017.
 * phoenix
 */
public class UploadQueueColumns implements BaseColumns {

    public static final String TABLENAME = "upload_queue";

    public static final String ACCOUNT_ID = "aid";

    public static final String DEST_ID1 = "dest_id1";

    public static final String DEST_ID2 = "dest_id2";

    public static final String DEST_ID3 = "dest_id3";

    public static final String METHOD = "method";

    public static final String STATUS = "status";

    public static final String ERROR_TEXT = "error_text";

    public static final String DATA = "raw_data";

}

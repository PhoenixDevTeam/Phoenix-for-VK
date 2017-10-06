package biz.dealnote.messenger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import biz.dealnote.messenger.db.column.UploadQueueColumns;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 27.01.2017.
 * phoenix
 */
public class UploadSqliteHelper extends SQLiteOpenHelper {

    private static final int V = 2;
    private static volatile UploadSqliteHelper instance;

    public static UploadSqliteHelper getInstance(Context context) {
        if(Objects.isNull(instance)){
            synchronized (UploadSqliteHelper.class){
                if(Objects.isNull(instance)){
                    instance = new UploadSqliteHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private UploadSqliteHelper(Context context) {
        super(context, "upload_queue.sqlite", null, V);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS [" + UploadQueueColumns.TABLENAME + "] (\n" +
                "  [" + UploadQueueColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + UploadQueueColumns.ACCOUNT_ID + "] INTEGER, " +
                "  [" + UploadQueueColumns.DEST_ID1 + "] INTEGER, " +
                "  [" + UploadQueueColumns.DEST_ID2 + "] INTEGER, " +
                "  [" + UploadQueueColumns.DEST_ID3 + "] INTEGER, " +
                "  [" + UploadQueueColumns.METHOD + "] INTEGER, " +
                "  [" + UploadQueueColumns.STATUS + "] INTEGER, " +
                "  [" + UploadQueueColumns.ERROR_TEXT + "] TEXT, " +
                "  [" + UploadQueueColumns.DATA + "] TEXT," +
                "  CONSTRAINT [] UNIQUE ([" + UploadQueueColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + UploadQueueColumns.TABLENAME);
        onCreate(db);
    }
}
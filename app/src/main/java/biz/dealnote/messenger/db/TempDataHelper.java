package biz.dealnote.messenger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import biz.dealnote.messenger.db.column.TempDataColumns;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public class TempDataHelper extends SQLiteOpenHelper {

    private static final Object lock = new Object();
    private static volatile TempDataHelper instance;

    public static TempDataHelper getInstance(Context context) {
        if(isNull(instance)){
            synchronized (lock){
                if(isNull(instance)){
                    instance = new TempDataHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private TempDataHelper(Context context) {
        super(context, "temp_app_data.sqlite", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTmpDataTable(db);
    }

    private void createTmpDataTable(SQLiteDatabase db){
        String sql = "CREATE TABLE IF NOT EXISTS [" + TempDataColumns.TABLENAME + "] (\n" +
                "  [" + TempDataColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + TempDataColumns.OWNER_ID + "] INTEGER, " +
                "  [" + TempDataColumns.SOURCE_ID + "] INTEGER, " +
                "  [" + TempDataColumns.DATA + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + TempDataColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
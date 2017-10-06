package biz.dealnote.messenger.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import biz.dealnote.messenger.db.column.LogColumns;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 27.01.2017.
 * phoenix
 */
public class LogSqliteHelper extends SQLiteOpenHelper {

    private static final int V = 1;
    private static volatile LogSqliteHelper instance;

    public static LogSqliteHelper getInstance(Context context) {
        if(Objects.isNull(instance)){
            synchronized (LogSqliteHelper.class){
                if(Objects.isNull(instance)){
                    instance = new LogSqliteHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private LogSqliteHelper(Context context) {
        super(context, "logs.sqlite", null, V);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS [" + LogColumns.TABLENAME + "] (\n" +
                "  [" + LogColumns._ID + "] INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "  [" + LogColumns.TYPE + "] INTEGER, " +
                "  [" + LogColumns.DATE + "] INTEGER, " +
                "  [" + LogColumns.TAG + "] TEXT, " +
                "  [" + LogColumns.BODY + "] TEXT, " +
                "  CONSTRAINT [] UNIQUE ([" + LogColumns._ID + "]) ON CONFLICT REPLACE);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LogColumns.TABLENAME);
        onCreate(db);
    }
}

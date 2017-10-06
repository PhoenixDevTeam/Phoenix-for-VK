package biz.dealnote.messenger.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.TempDataHelper;
import biz.dealnote.messenger.db.column.TempDataColumns;
import biz.dealnote.messenger.db.interfaces.ITempDataStore;
import biz.dealnote.messenger.db.serialize.ISerializeAdapter;
import biz.dealnote.messenger.util.Exestime;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public class TempDataStore implements ITempDataStore {

    private final Context app;

    TempDataStore(Context context) {
        this.app = context.getApplicationContext();
    }

    private TempDataHelper helper() {
        return TempDataHelper.getInstance(app);
    }

    private static final String[] PROJECTION = {
            TempDataColumns._ID, TempDataColumns.OWNER_ID, TempDataColumns.SOURCE_ID, TempDataColumns.DATA};


    @Override
    public <T> Single<List<T>> getData(int ownerId, int sourceId, ISerializeAdapter<T> serializer) {
        return Single.fromCallable(() -> {
            long start = System.currentTimeMillis();

            String where = TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?";
            String[] args = {String.valueOf(ownerId), String.valueOf(sourceId)};

            Cursor cursor = helper().getReadableDatabase().query(TempDataColumns.TABLENAME,
                    PROJECTION, where, args, null, null, null);

            List<T> data = new ArrayList<>(cursor.getCount());

            try {
                while (cursor.moveToNext()) {
                    String raw = cursor.getString(3);
                    data.add(serializer.deserialize(raw));
                }
            } finally {
                cursor.close();
            }

            Exestime.log("TempDataStore.getData", start, "count: " + data.size());
            return data;
        });
    }

    @Override
    public <T> Completable put(int ownerId, int sourceId, List<T> data, ISerializeAdapter<T> serializer) {
        return Completable.create(emitter -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper().getWritableDatabase();

            db.beginTransaction();

            try {
                // clear
                db.delete(TempDataColumns.TABLENAME,
                        TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?",
                        new String[]{String.valueOf(ownerId), String.valueOf(sourceId)});

                for (T t : data) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    ContentValues cv = new ContentValues();
                    cv.put(TempDataColumns.OWNER_ID, ownerId);
                    cv.put(TempDataColumns.SOURCE_ID, sourceId);
                    cv.put(TempDataColumns.DATA, serializer.serialize(t));

                    db.insert(TempDataColumns.TABLENAME, null, cv);
                }

                if (!emitter.isDisposed()) {
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }

            Exestime.log("TempDataStore.put", start, "count: " + data.size());

            emitter.onComplete();
        });
    }

    @Override
    public Completable delete(int ownerId) {
        return Completable.fromAction(() -> {
            long start = System.currentTimeMillis();
            int count = helper().getWritableDatabase().delete(TempDataColumns.TABLENAME,
                    TempDataColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)});
            Exestime.log("TempDataStore.delete", start, "count: " + count);
        });
    }
}
package biz.dealnote.messenger.db;

import android.database.Cursor;

/**
 * Created by admin on 21.03.2017.
 * phoenix
 */
public interface MapFunction<T> {
    T map(Cursor cursor);
}

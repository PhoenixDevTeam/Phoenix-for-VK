package biz.dealnote.messenger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import biz.dealnote.messenger.db.column.DialogsColumns;
import biz.dealnote.messenger.db.column.MessageColumns;

public class DialogsHelper {

    public static void changeChatTitle(Context context, int aid, int peerId, String title) {
        ContentValues cv = new ContentValues();
        cv.put(DialogsColumns.TITLE, title);
        context.getContentResolver().update(MessengerContentProvider.getDialogsContentUriFor(aid), cv,
                DialogsColumns._ID + " = ?", new String[]{String.valueOf(peerId)});
    }

    public static int checkUnreadCount(Context context, int accountId, int peerId, Integer count) {
        int result = 0;
        if (count == null) {
            Cursor cursor = DBHelper.getInstance(context, accountId)
                    .getReadableDatabase()
                    .rawQuery("SELECT COUNT(" + MessageColumns._ID + ") FROM " + MessageColumns.TABLENAME +
                                    " WHERE " + MessageColumns.PEER_ID + " = ?" +
                                    " AND " + MessageColumns.READ_STATE + " = ? " +
                                    " AND " + MessageColumns.OUT + " = ? " +
                                    " AND " + MessageColumns.ATTACH_TO + " = ? " +
                                    " AND " + MessageColumns.DELETED + " = ?",
                            new String[]{String.valueOf(peerId), "0", "0", "0", "0"});
            if (cursor.moveToNext()) {
                result = cursor.getInt(0);
            }

            cursor.close();
        } else {
            result = count;
        }

        ContentValues cv = new ContentValues();
        cv.put(DialogsColumns.UNREAD, result);
        context.getContentResolver().update(MessengerContentProvider.getDialogsContentUriFor(accountId),
                cv, DialogsColumns._ID + " = ?", new String[]{String.valueOf(peerId)});
        return result;
    }
}
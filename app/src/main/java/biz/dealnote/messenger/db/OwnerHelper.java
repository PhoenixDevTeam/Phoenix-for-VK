package biz.dealnote.messenger.db;

import android.content.Context;
import android.database.Cursor;

import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.UserColumns;

public class OwnerHelper {

    public static String loadOwnerFullName(Context context, int aid, int ownerId){
        if(ownerId == 0) return null;

        String result = null;
        if(ownerId > 0){
            Cursor uCursor = context.getContentResolver().query(MessengerContentProvider.getUserContentUriFor(aid),
                    null, UserColumns._ID + " = ?", new String[]{String.valueOf(ownerId)}, null);
            if (uCursor != null) {
                if(uCursor.moveToNext()){
                    result = uCursor.getString(uCursor.getColumnIndex(UserColumns.FIRST_NAME)) +
                            " " + uCursor.getString(uCursor.getColumnIndex(UserColumns.LAST_NAME));
                }

                uCursor.close();
            }
        } else {
            Cursor gCursor = context.getContentResolver().query(MessengerContentProvider.getGroupsContentUriFor(aid),
                    null, GroupColumns._ID + " = ?", new String[]{String.valueOf(-ownerId)}, null);

            if (gCursor != null) {
                if(gCursor.moveToNext()){
                    result = gCursor.getString(gCursor.getColumnIndex(GroupColumns.NAME));
                }

                gCursor.close();
            }
        }

        return result;
    }
}

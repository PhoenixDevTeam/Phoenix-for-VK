package biz.dealnote.messenger.db;

import android.content.Context;
import android.database.Cursor;

import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.UserColumns;

public class OwnersHelper {

    public static ResolveDomailResponse resolveScreenName(Context context, int aid, String name){
        ResolveDomailResponse result = null;

        Cursor uCursor = context.getContentResolver().query(MessengerContentProvider.getUserContentUriFor(aid),
                new String[]{UserColumns._ID, UserColumns.DOMAIN},
                UserColumns.DOMAIN + " LIKE ?", new String[]{name}, null);
        if(uCursor != null){
            if(uCursor.moveToNext()){
                int uid = uCursor.getInt(uCursor.getColumnIndex(UserColumns._ID));

                result = new ResolveDomailResponse();
                result.type = ResolveDomailResponse.TYPE_USER;
                result.object_id = String.valueOf(uid);
            }

            uCursor.close();
        }

        if(result != null){
            return result;
        }

        Cursor gCursor = context.getContentResolver().query(MessengerContentProvider.getGroupsContentUriFor(aid),
                new String[]{GroupColumns._ID, GroupColumns.SCREEN_NAME},
                GroupColumns.SCREEN_NAME + " LIKE ?", new String[]{name}, null);

        if(gCursor != null){
            if(gCursor.moveToNext()){
                int gid = gCursor.getInt(gCursor.getColumnIndex(GroupColumns._ID));

                result = new ResolveDomailResponse();
                result.type = ResolveDomailResponse.TYPE_GROUP;
                result.object_id = String.valueOf(gid);
            }

            gCursor.close();
        }

        return result;
    }
}
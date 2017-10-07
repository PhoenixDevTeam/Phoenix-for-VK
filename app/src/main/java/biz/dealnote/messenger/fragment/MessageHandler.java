package biz.dealnote.messenger.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Collection;

import biz.dealnote.messenger.db.DialogsHelper;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.MessageColumns;

import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class MessageHandler {

    /**
     * Пометить сообщения как прочитанные
     *
     * @param ids            идентификаторы сообщений
     * @param startMessageId при передаче этого параметра будут помечены как прочитанные все сообщения начиная с данного.
     */
    public static int markMessagesAsRead(Context context, int accountId, Collection<Integer> ids, Integer peerId, Integer startMessageId) {
        if (safeIsEmpty(ids) && peerId == null) {
            throw new IllegalArgumentException("User_id, chat_id and messages_ids can't be unspecified the same time");
        }

        Uri messagesWithAccountUri = MessengerContentProvider.getMessageContentUriFor(accountId);

        // Определение user_id и chat_id, если они были не указаны
        if (peerId == null) {
            String[] projection = {MessageColumns.PEER_ID};
            Cursor cursor = context.getContentResolver().query(messagesWithAccountUri, projection,
                    MessageColumns._ID + " IN (" + TextUtils.join(",", ids) + ")", null, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    peerId = cursor.getInt(cursor.getColumnIndex(MessageColumns.PEER_ID));
                }

                cursor.close();
            }
        }

        int count;
        ContentValues cv = new ContentValues();
        cv.put(MessageColumns.READ_STATE, true);

        if (!safeIsEmpty(ids)) {
            count = context.getContentResolver().update(messagesWithAccountUri, cv,
                    MessageColumns._ID + " IN (" + TextUtils.join(",", ids) + ") " +
                            " AND " + MessageColumns.OUT + " = ? " +
                            " AND " + MessageColumns.DELETED + " = ? " +
                            " AND " + MessageColumns.READ_STATE + " = ? " +
                            " AND " + MessageColumns.ATTACH_TO + " = ?",
                    new String[]{"0", "0", "0", "0"});
        } else {
            String[] selectionArgs;
            if (startMessageId != null) {
                selectionArgs = new String[]{"0", "0", "0", "0", String.valueOf(peerId), String.valueOf(startMessageId)};
            } else {
                selectionArgs = new String[]{"0", "0", "0", "0", String.valueOf(peerId)};
            }

            count = context.getContentResolver().update(messagesWithAccountUri, cv,
                    MessageColumns.OUT + " = ? " +
                            " AND " + MessageColumns.ATTACH_TO + " = ? " +
                            " AND " + MessageColumns.DELETED + " = ? " +
                            " AND " + MessageColumns.READ_STATE + " = ? " +
                            " AND " + MessageColumns.PEER_ID + " = ? " +
                            (startMessageId != null ? " AND " + MessageColumns._ID + " >= ?" : ""), selectionArgs);
        }

        // Если коллекция id и код начального сообщения не
        // указан, то прочитать все сообщения, тоесть сбросить счетчик
        Integer unreadCount = (ids == null && startMessageId == null ? 0 : null);

        DialogsHelper.checkUnreadCount(context, accountId, peerId, unreadCount);
        return count;
    }
}

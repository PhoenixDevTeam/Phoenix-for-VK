package biz.dealnote.messenger.db;

import android.content.ContentProviderOperation;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsResetUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsSetUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOfflineUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOnlineUpdate;
import biz.dealnote.messenger.db.column.MessageColumns;
import biz.dealnote.messenger.db.column.UserColumns;

import static biz.dealnote.messenger.util.Utils.hasFlag;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class LongPollOperation {

    /**
     * Получение списка операций для сохранения уведомлений о том,
     * что для сообщений были ОТМЕНЕНЫ атрибуты
     *
     * @param updates уведомления
     * @return список операций
     */
    public static ArrayList<ContentProviderOperation> forMessagesFlagReset(int accountId, @NonNull List<MessageFlagsResetUpdate> updates) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(updates.size());
        for (MessageFlagsResetUpdate update : updates) {
            ContentValues cv = new ContentValues();

            if(hasFlag(update.mask, VKApiMessage.FLAG_DELETED)){
                cv.put(MessageColumns.DELETED, 0);
            }

            if(hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)){
                cv.put(MessageColumns.IMPORTANT, 0);
            }

            //if(hasFlag(update.mask, VKApiMessage.FLAG_UNREAD)){
                //cv.put(MessageColumns.READ_STATE, 1);
            //}

            if(cv.size() > 0){
                String where = MessageColumns._ID + " = ?";
                String[] args = {String.valueOf(update.message_id)};
                operations.add(ContentProviderOperation
                        .newUpdate(MessengerContentProvider.getMessageContentUriFor(accountId))
                        .withSelection(where, args)
                        .withValues(cv)
                        .build());
            }
        }

        return operations;
    }

    /**
     * Получение списка операций для сохранения уведомлений о том,
     * что для сообщений были ПРИМЕНЕНЫ атрибуты
     *
     * @param updates уведомления
     * @return список операций
     */
    public static ArrayList<ContentProviderOperation> forMessagesFlagSet(int accountId, @NonNull List<MessageFlagsSetUpdate> updates) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>(updates.size());
        for (MessageFlagsSetUpdate update : updates) {
            ContentValues cv = new ContentValues();

            if(hasFlag(update.mask, VKApiMessage.FLAG_DELETED)){
                cv.put(MessageColumns.DELETED, 1);
            }

            if(hasFlag(update.mask, VKApiMessage.FLAG_IMPORTANT)){
                cv.put(MessageColumns.IMPORTANT, 1);
            }

            if(cv.size() != 0){
                String where = MessageColumns._ID + " = ?";
                String[] args = {String.valueOf(update.message_id)};
                operations.add(ContentProviderOperation
                        .newUpdate(MessengerContentProvider.getMessageContentUriFor(accountId))
                        .withSelection(where, args)
                        .withValues(cv)
                        .build());
            }
        }

        return operations;
    }

    /**
     * Получение списка операций для сохранения уведомлений о том, что пользователь стал "онлайн" или "оффлайн"
     *
     * @param offline уведомления об "оффлайн"
     * @param online  уведомления об "онлайн"
     * @return список операций
     */
    public static ArrayList<ContentProviderOperation> forUserActivityUpdates(int accountId, List<UserIsOfflineUpdate> offline, List<UserIsOnlineUpdate> online) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>((offline != null ? offline.size() : 0) + (online != null ? online.size() : 0));

        if (!safeIsEmpty(offline)) {
            ContentValues cv = new ContentValues();
            cv.put(UserColumns.ONLINE, 0);
            for (UserIsOfflineUpdate u : offline) {
                operations.add(ContentProviderOperation
                        .newUpdate(MessengerContentProvider.getUserContentUriFor(accountId))
                        .withSelection(UserColumns._ID + " = ?", new String[]{String.valueOf(Math.abs(u.user_id))})
                        .withValues(cv)
                        .build());
            }
        }

        if (!safeIsEmpty(online)) {
            ContentValues cv = new ContentValues();
            cv.put(UserColumns.ONLINE, 1);
            for (UserIsOnlineUpdate u : online) {
                cv.put(UserColumns.PLATFORM, u.extra);

                operations.add(ContentProviderOperation
                        .newUpdate(MessengerContentProvider.getUserContentUriFor(accountId))
                        .withSelection(UserColumns._ID + " = ?", new String[]{String.valueOf(Math.abs(u.user_id))})
                        .withValues(cv)
                        .build());
            }
        }

        return operations;
    }
}

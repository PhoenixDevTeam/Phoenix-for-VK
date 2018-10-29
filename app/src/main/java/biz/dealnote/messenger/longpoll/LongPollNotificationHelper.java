package biz.dealnote.messenger.longpoll;

import android.content.Context;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Utils.hasFlag;
import static biz.dealnote.messenger.util.Utils.isEmpty;

public class LongPollNotificationHelper {

    public static final String TAG = LongPollNotificationHelper.class.getSimpleName();

    /**
     * Действие при добавлении нового сообщения в диалог или чат
     *
     * @param message нотификация с сервера
     */
    public static void notifyAbountNewMessage(Context context, final Message message) {
        if (message.isOut()) {
            return;
        }

        //if (message.isRead()) {
        //    return;
        //}

        //boolean needSendNotif = needNofinicationFor(message.getAccountId(), message.getPeerId());
        //if(!needSendNotif){
        //    return;
        //}

        String messageText = isEmpty(message.getDecryptedBody()) ? (isEmpty(message.getBody())
                ? context.getString(R.string.attachments) : message.getBody()) : message.getDecryptedBody();

        notifyAbountNewMessage(context, message.getAccountId(), messageText, message.getPeerId(), message.getId(), message.getDate());
    }

    private static void notifyAbountNewMessage(Context context, int accountId, String body, int peerId, int messageId, long date){
        int mask = Settings.get().notifications().getNotifPref(accountId, peerId);
        if (!hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            return;
        }

        if (Settings.get().accounts().getCurrent() != accountId) {
            Logger.d(TAG, "notifyAbountNewMessage, Attempting to send a notification does not in the current account!!!");
            return;
        }

        NotificationHelper.notifNewMessage(context, accountId, body, peerId, messageId, date);
    }
}
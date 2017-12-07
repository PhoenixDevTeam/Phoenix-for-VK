package biz.dealnote.messenger.longpoll;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.activity.QuickAnswerActivity;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.ChatEntryFetcher;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.hasFlag;

public class NotificationHelper {

    /**
     * Отображение уведомления в statusbar о новом сообщении.
     * Этот метод сначала в отдельном потоке получает всю необходимую информацию для отображения
     *
     * @param context   контекст
     * @param text      текст сообщения
     * @param messageId идентификатор сообщения
     */
    public static void notifNewMessage(final Context context, final int accountId, final String text, final int peerId, final int messageId, final long messageTime) {
        ChatEntryFetcher.getRx(context, accountId, peerId)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(response -> showNotification(context, accountId, response.icon, response.title, text, messageId, peerId,
                        response.last_seen, messageTime, response.img), throwable -> {/*ignore*/});
    }

    /**
     * Отображение уведомления в statusbar о новом сообщении
     *
     * @param context  контекст
     * @param img      аватар или изображение беседы
     * @param title    имя собеседника или название беседы
     * @param body     текст сообщения
     * @param mid      идентификатор сообщения
     * @param lastSeen в сети ли пользователь(не для групповых бесед)
     * @param imgUrl   ссылка на аватар или изображение беседы
     */
    private static void showNotification(Context context, int accountId, Bitmap img, String title, String body, int mid,
                                         int peerId, long lastSeen, long messageSentTime, String imgUrl) {
        boolean hideBody = Settings.get()
                .security()
                .needHideMessagesBodyForNotif();

        String text = hideBody ? context.getString(R.string.message_text_is_not_available) : body;

        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Objects.isNull(nManager)){
            return;
        }

        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getChatMessageChannel(context));
            nManager.createNotificationChannel(AppNotificationChannels.getGroupChatMessageChannel(context));
        }

        String channelId = Peer.isGroupChat(peerId) ? AppNotificationChannels.GROUP_CHAT_MESSAGE_CHANNEL_ID :
                AppNotificationChannels.CHAT_MESSAGE_CHANNEL_ID;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(img)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true);

        int notificationMask = Settings.get()
                .notifications()
                .getNotifPref(accountId, peerId);

        if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_HIGH_PRIORITY)) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        //Our quickreply
        Intent intentQuick = QuickAnswerActivity.forStart(context, accountId, peerId, text, mid, lastSeen, messageSentTime, imgUrl, title);
        PendingIntent quickPendingIntent = PendingIntent.getActivity(context, mid, intentQuick, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionCustomReply = new NotificationCompat.Action(R.drawable.reply, context.getString(R.string.quick_answer_title), quickPendingIntent);

        //System reply. Works only on Wear (20 Api) and N+
        RemoteInput remoteInput = new RemoteInput.Builder(Extra.BODY)
                .setLabel(context.getResources().getString(R.string.reply))
                .build();

        Intent directIntent = SendService.intentForAddMessage(context, accountId, peerId);
        PendingIntent directPendingIntent = PendingIntent.getService(context, mid, directIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action actionDirectReply = new NotificationCompat.Action.Builder
                (/*may be missing in some cases*/ R.drawable.reply,
                        context.getResources().getString(R.string.reply), directPendingIntent)
                .addRemoteInput(remoteInput)
                .build();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);

        final Peer peer = new Peer(peerId).setTitle(title).setAvaUrl(imgUrl);

        Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, peer);
        intent.putExtra(Extra.PLACE, chatPlace);

        PendingIntent contentIntent = PendingIntent.getActivity(context, mid, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);

        if (!Utils.hasNougat()) {
            builder.addAction(actionCustomReply);
        } else {
            builder.addAction(actionDirectReply);
        }

        //Для часов игнорирует все остальные action, тем самым убирает QuickReply
        builder.extend(new NotificationCompat.WearableExtender().addAction(actionDirectReply));

        Notification notification = builder.build();

        if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_LED)) {
            notification.ledARGB = 0xFF0000FF;
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
            notification.ledOnMS = 100;
            notification.ledOffMS = 1000;
        }

        if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_VIBRO)) {
            //notification.defaults |= Notification.DEFAULT_VIBRATE;
            notification.vibrate = Settings.get()
                    .notifications()
                    .getVibrationLength();
        }

        if (hasFlag(notificationMask, ISettings.INotificationSettings.FLAG_SOUND)) {
            notification.sound = findNotificationSound();
        }

        nManager.notify(createPeerTagFor(accountId, peerId), NOTIFICATION_MESSAGE, notification);

        if (Settings.get().notifications().isQuickReplyImmediately()) {
            Intent startQuickReply = QuickAnswerActivity.forStart(context, accountId, peerId, text, mid, lastSeen, messageSentTime, imgUrl, title);
            startQuickReply.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startQuickReply.putExtra(QuickAnswerActivity.EXTRA_FOCUS_TO_FIELD, false);
            startQuickReply.putExtra(QuickAnswerActivity.EXTRA_LIVE_DELAY, true);
            context.startActivity(startQuickReply);
        }
    }

    private static String createPeerTagFor(int aid, int peerId) {
        return aid + "_" + peerId;
    }

    public static final int NOTIFICATION_MESSAGE = 62;
    public static final int NOTIFICATION_WALL_POST_ID = 63;
    public static final int NOTIFICATION_REPLY_ID = 64;
    public static final int NOTIFICATION_COMMENT_ID = 65;
    public static final int NOTIFICATION_WALL_PUBLISH_ID = 66;
    public static final int NOTIFICATION_FRIEND_ID = 67;
    public static final int NOTIFICATION_FRIEND_ACCEPTED_ID = 68;
    public static final int NOTIFICATION_GROUP_INVITE_ID = 69;
    public static final int NOTIFICATION_NEW_POSTS_ID = 70;
    public static final int NOTIFICATION_LIKE = 71;
    public static final int NOTIFICATION_BIRTHDAY = 72;

    public static Uri findNotificationSound() {
        try {
            return Uri.parse(Settings.get()
                    .notifications()
                    .getNotificationRingtone());
        } catch (Exception ignored) {
            return Uri.parse(Settings.get()
                    .notifications()
                    .getDefNotificationRingtone());
        }
    }

    private static NotificationManager getService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void tryCancelNotificationForPeer(Context context, int accountId, int peerId) {
        int mask = Settings.get()
                .notifications()
                .getNotifPref(accountId, peerId);

        if (hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            getService(context).cancel(NotificationHelper.createPeerTagFor(accountId, peerId),
                    NotificationHelper.NOTIFICATION_MESSAGE);
        }
    }
}
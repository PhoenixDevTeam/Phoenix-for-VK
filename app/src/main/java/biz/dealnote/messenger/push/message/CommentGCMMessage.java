package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.CommentedType;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.push.NotificationUtils;
import biz.dealnote.messenger.push.OwnerInfo;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;

public class CommentGCMMessage {

    /**
     * Идентификатор пользователя
     */
    private int from_id;

    /**
     * Идентификатор комментария
     */
    private int reply_id;

    //private int sex;
    //public long from;
    private String text;
    private String place;
    //private String type;

    //extras: Bundle[{google.sent_time=1477925617791, from_id=175895893, reply_id=3686, sex=2,
    // text=да, type=comment, place=wall25651989_3499, google.message_id=0:1477925617795994%8c76e97a38a5ee5f, _genSrv=833239, sandbox=0, collapse_key=comment}]

    public static CommentGCMMessage fromBundle(@NonNull Bundle bundle) {
        CommentGCMMessage message = new CommentGCMMessage();
        message.from_id = NotificationUtils.optInt(bundle, "from_id");
        message.reply_id = NotificationUtils.optInt(bundle, "reply_id");
        //message.sex = optInt(bundle, "sex");
        //message.from = optLong(bundle, "google.sent_time");
        message.text = bundle.getString("text");
        //message.type = bundle.getString("type");
        message.place = bundle.getString("place");
        return message;
    }

    public void notify(Context context, int accountId) {
        if (!Settings.get()
                .notifications()
                .isCommentsNotificationsEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(context, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo), throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, OwnerInfo ownerInfo) {
        String url = "vk.com/" + place;
        Commented commented = LinkHelper.findCommentedFrom(url);

        if (commented == null) {
            return;
        }

        String subText = null;
        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                subText = context.getString(R.string.commented_to_photo);
                break;
            case CommentedType.VIDEO:
                subText = context.getString(R.string.commented_to_video);
                break;
            case CommentedType.POST:
                subText = context.getString(R.string.commented_to_post);
                break;
            case CommentedType.TOPIC:
                // not supported
                break;
        }

        if (subText == null) {
            return;
        }

        Spannable snannedText = OwnerLinkSpanFactory.withSpans(text, true, false, null);
        String targetText = snannedText == null ? null : snannedText.toString();

        Owner owner = ownerInfo.getOwner();

        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getCommentsChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.COMMENTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(ownerInfo.getAvatar())
                .setContentTitle(owner.getFullName())
                .setContentText(targetText)
                .setSubText(subText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(targetText))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getCommentsPlace(aid, commented, reply_id));
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, reply_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();

        configOtherPushNotification(notification);
        nManager.notify(place, NotificationHelper.NOTIFICATION_COMMENT_ID, notification);
    }
}
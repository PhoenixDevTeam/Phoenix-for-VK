package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.push.NotificationUtils;
import biz.dealnote.messenger.push.OwnerInfo;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;

public class ReplyGCMMessage {

    private static final String TAG = ReplyGCMMessage.class.getSimpleName();

    //{from_id=175895893, reply_id=4908, first_name=Руслан, sex=2, from=376771982493,
    // text=[id25651989|Руслан], тест, type=reply, place=wall-72124992_4688, sandbox=0,
    // collapse_key=reply, last_name=Колбаса

    private int from_id;
    private int reply_id;
    //public String firstName;
    //private int sex;
    //public long from;
    private String text;
    private String place;
    //public String lastName;
    //private String type;

    public static ReplyGCMMessage fromBundle(@NonNull Bundle bundle){
        ReplyGCMMessage message = new ReplyGCMMessage();
        message.from_id = NotificationUtils.optInt(bundle, "from_id");
        message.reply_id = NotificationUtils.optInt(bundle, "reply_id");
        //message.sex = optInt(bundle, "sex");
        //message.firstName = bundle.getString("first_name");
        //message.lastName = bundle.getString("last_name");
        //message.from = optLong(bundle, "from");
        message.text = bundle.getString("text");
        //message.type = bundle.getString("type");
        message.place = bundle.getString("place");
        return message;
    }

    public void notify(final Context context, int accountId){
        if (!Settings.get()
                .notifications()
                .isReplyNotifEnabled()) {
            return;
        }

        Context app = context.getApplicationContext();
        OwnerInfo.getRx(app, accountId, from_id)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(app, ownerInfo.getOwner(), ownerInfo.getAvatar()), throwable -> {/*ignore*/});
    }

    private void notifyImpl(Context context, @NonNull Owner owner, Bitmap bitmap){
        String url = "vk.com/" + place;
        Commented commented = LinkHelper.findCommentedFrom(url);

        if(commented == null){
            Logger.e(TAG, "Unknown place: " + place);
            return;
        }

        Spannable snannedText = OwnerLinkSpanFactory.withSpans(text, true, false, null);
        String targetText = snannedText == null ? null : snannedText.toString();

        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getCommentsChannel(context));
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.COMMENTS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(bitmap)
                .setContentTitle(owner.getFullName())
                .setContentText(targetText)
                .setSubText(context.getString(R.string.in_reply_to_your_comment))
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
        nManager.notify(place, NotificationHelper.NOTIFICATION_REPLY_ID, notification);
    }
}

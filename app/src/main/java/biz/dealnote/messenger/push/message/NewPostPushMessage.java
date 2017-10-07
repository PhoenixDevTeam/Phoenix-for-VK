package biz.dealnote.messenger.push.message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.longpoll.AppNotificationChannels;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.push.NotificationScheduler;
import biz.dealnote.messenger.push.OwnerInfo;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.push.NotificationUtils.configOtherPushNotification;
import static biz.dealnote.messenger.push.NotificationUtils.optInt;
import static biz.dealnote.messenger.util.Utils.stringEmptyIfNull;

/**
 * Created by ruslan.kolbasa on 10.01.2017.
 * phoenix
 */
public class NewPostPushMessage {

    //onMessage, from: 237327763482, collapseKey: new_post, extras: Bundle[{google.sent_time=1484043151928, from_id=-114075457, post_id=11, text=Добрый вечер, type=new_post, google.message_id=0:1484043151930717%8c76e97a5216abdf, group_name=Phoenix for VK Closed, _genSrv=518507, sandbox=0, collapse_key=new_post, post_type=group_status}]
            //01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: google.sent_time, value: 1484043151928, class: class java.lang.Long
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: from_id, value: -114075457, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: post_id, value: 11, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: text, value: Добрый вечер, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: type, value: new_post, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: google.message_id, value: 0:1484043151930717%8c76e97a5216abdf, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: group_name, value: Phoenix for VK Closed, class: class java.lang.String
//01-10 12:12:31.581 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: _genSrv, value: 518507, class: class java.lang.String
//01-10 12:12:31.582 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: sandbox, value: 0, class: class java.lang.String
//01-10 12:12:31.582 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: collapse_key, value: new_post, class: class java.lang.String
//01-10 12:12:31.582 28251-8113/biz.dealnote.phoenix D/MyGcmListenerService: key: post_type, value: group_status, class: class java.lang.String

    private final int accountId;

    private final int fromId;

    private final int postId;

    private final String text;

    private final String groupName;

    private final String firstName;

    private final String lastName;

    //private String postType;

    public NewPostPushMessage(int accountId, Bundle bundle){
        this.accountId = accountId;
        this.fromId = optInt(bundle, "from_id");
        this.postId = optInt(bundle, "post_id");
        this.text = bundle.getString("text");
        this.groupName = bundle.getString("group_name");
        this.firstName = bundle.getString("first_name");
        this.lastName = bundle.getString("last_name");
        //this.postType = bundle.getString("post_type"); // group_status, status
    }

    public void notifyIfNeed(Context context){
        if(fromId == 0){
            Logger.wtf("NewPostPushMessage", "from_id is NULL!!!");
            return;
        }

        if (!Settings.get()
                .notifications()
                .isNewPostsNotificationEnabled()) {
            return;
        }

        OwnerInfo.getRx(context, accountId, fromId)
                .subscribeOn(NotificationScheduler.INSTANCE)
                .subscribe(ownerInfo -> notifyImpl(context, ownerInfo), ignored -> {});
    }

    private void notifyImpl(Context context, OwnerInfo info){
        String ownerName = fromId > 0 ? (stringEmptyIfNull(firstName) + " " + stringEmptyIfNull(lastName)) : groupName;
        final NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Utils.hasOreo()){
            nManager.createNotificationChannel(AppNotificationChannels.getNewPostChannel(context));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AppNotificationChannels.NEW_POST_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify_statusbar)
                .setLargeIcon(info.getAvatar())
                .setContentTitle(context.getString(R.string.new_post_title))
                .setContentText(context.getString(R.string.new_post_was_published_in, ownerName))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true);

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Extra.PLACE, PlaceFactory.getPostPreviewPlace(accountId, postId, fromId));

        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, fromId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();

        configOtherPushNotification(notification);

        nManager.notify(String.valueOf(fromId), NotificationHelper.NOTIFICATION_NEW_POSTS_ID, notification);
    }
}
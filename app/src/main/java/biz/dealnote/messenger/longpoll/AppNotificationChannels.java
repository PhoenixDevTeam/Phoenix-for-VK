package biz.dealnote.messenger.longpoll;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.support.annotation.RequiresApi;

import biz.dealnote.messenger.R;

/**
 * Created by Emin on 10/4/2017.
 */
public class AppNotificationChannels {
    public static final String CHAT_MESSAGE_CHANNEL_ID = "chat_message_channel";
    public static final String GROUP_CHAT_MESSAGE_CHANNEL_ID = "group_chat_message_channel";
    public static final String KEY_EXCHANGE_CHANNEL_ID = "key_exchange_channel";
    public static final String LIKES_CHANNEL_ID = "likes_channel";
    public static final String AUDIO_CHANNEL_ID = "audio_channel";
    public static final String COMMENTS_CHANNEL_ID = "comments_channel";
    public static final String NEW_POST_CHANNEL_ID = "new_post_channel";
    public static final String GROUP_INVITES_CHANNEL_ID = "group_invites_channel";
    public static final String FRIEND_REQUESTS_CHANNEL_ID = "friend_requests_channel";
    public static final String BIRTHDAYS_CHANNEL_ID = "birthdays_channel";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static final AudioAttributes ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
            .build();

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getChatMessageChannel(Context context){
        String channelName = context.getString(R.string.message_channel);

        NotificationChannel channel = new NotificationChannel(CHAT_MESSAGE_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(NotificationHelper.findNotificationSound(), ATTRIBUTES);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getGroupChatMessageChannel(Context context){
        String channelName = context.getString(R.string.group_message_channel);
        NotificationChannel channel = new NotificationChannel(GROUP_CHAT_MESSAGE_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(NotificationHelper.findNotificationSound(), ATTRIBUTES);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getKeyExchangeChannel(Context context){
        String channelName = context.getString(R.string.key_exchange_channel);
        NotificationChannel channel = new NotificationChannel(KEY_EXCHANGE_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(false);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getLikesChannel(Context context){
        String channelName = context.getString(R.string.likes_channel);
        NotificationChannel channel = new NotificationChannel(LIKES_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getAudioChannel(Context context){
        String channelName = context.getString(R.string.audio_channel);
        NotificationChannel channel = new NotificationChannel(AUDIO_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(false);
        channel.enableVibration(false);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getCommentsChannel(Context context){
        String channelName = context.getString(R.string.comment_channel);
        NotificationChannel channel = new NotificationChannel(COMMENTS_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getNewPostChannel(Context context){
        String channelName = context.getString(R.string.new_posts_channel);
        NotificationChannel channel = new NotificationChannel(NEW_POST_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getGroupInvitesChannel(Context context){
        String channelName = context.getString(R.string.group_invites_channel);
        NotificationChannel channel = new NotificationChannel(GROUP_INVITES_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getFriendRequestsChannel(Context context){
        String channelName = context.getString(R.string.friend_requests_channel);
        NotificationChannel channel = new NotificationChannel(FRIEND_REQUESTS_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static NotificationChannel getBirthdaysChannel(Context context){
        String channelName = context.getString(R.string.birthdays);
        NotificationChannel channel = new NotificationChannel(BIRTHDAYS_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
        channel.enableLights(true);
        channel.enableVibration(true);
        return channel;
    }
}
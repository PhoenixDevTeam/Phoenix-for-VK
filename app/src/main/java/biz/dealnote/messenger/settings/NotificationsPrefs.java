package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.util.Utils;

public class NotificationsPrefs implements ISettings.INotificationSettings {

    private static final String KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
    private static final String NOTIF_PREF_NAME = "biz.dealnote.notifpref";
    private static final String KEY_VIBRO_LENGTH = "vibration_length";

    private Context app;
    private SharedPreferences preferences;

    NotificationsPrefs(Context context) {
        app = context.getApplicationContext();
        preferences = context.getSharedPreferences(NOTIF_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void setNotifPref(int aid, int peerid, int mask) {
        preferences.edit()
                .putInt(keyFor(aid, peerid), mask)
                .apply();
    }

    private boolean isOtherNotificationsEnable(){
        return Utils.hasFlag(getOtherNotificationMask(), FLAG_SHOW_NOTIF);
    }

    @Override
    public int getOtherNotificationMask() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        int mask = 0;
        if(preferences.getBoolean("other_notifications_enable", true)){
            mask = mask + FLAG_SHOW_NOTIF;
        }

        if(preferences.getBoolean("other_notif_sound", true)){
            mask = mask + FLAG_SOUND;
        }

        if(preferences.getBoolean("other_notif_vibration", true)){
            mask = mask + FLAG_VIBRO;
        }

        if(preferences.getBoolean("other_notif_led", true)){
            mask = mask + FLAG_LED;
        }
        return mask;
    }

    @Override
    public boolean isCommentsNotificationsEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_comment_notification", true);
    }

    @Override
    public boolean isFriendRequestAcceptationNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("friend_request_accepted_notification", true);
    }

    @Override
    public boolean isNewFollowerNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_follower_notification", true);
    }

    @Override
    public boolean isWallPublishNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("wall_publish_notification", true);
    }

    @Override
    public boolean isGroupInvitedNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("group_invited_notification", true);
    }

    @Override
    public boolean isReplyNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("reply_notification", true);
    }

    @Override
    public boolean isNewPostOnOwnWallNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_wall_post_notification", true);
    }

    @Override
    public boolean isNewPostsNotificationEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("new_posts_notification", true);
    }

    @Override
    public boolean isBirtdayNotifEnabled() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("birtday_notification", true);
    }

    @Override
    public boolean isLikeNotificationEnable() {
        return isOtherNotificationsEnable() && PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean("likes_notification", true);
    }

    @Override
    public Uri getFeedbackRingtoneUri() {
        String path = "android.resource://" + app.getPackageName() + "/" + R.raw.feedback_sound;
        return Uri.parse(path);
    }

    @Override
    public String getDefNotificationRingtone() {
        return "android.resource://" + app.getPackageName() + "/" + R.raw.notification_sound;
    }

    @Override
    public String getNotificationRingtone() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getString(KEY_NOTIFICATION_RINGTONE, getDefNotificationRingtone());
    }

    @Override
    public void setNotificationRingtoneUri(String path) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putString(KEY_NOTIFICATION_RINGTONE, path)
                .apply();
    }

    @Override
    public long[] getVibrationLength() {
        switch (PreferenceManager.getDefaultSharedPreferences(app)
                .getString(KEY_VIBRO_LENGTH, "4")) {
            case "0":
                return new long[]{0, 300};
            case "1":
                return new long[]{0, 400};
            case "2":
                return new long[]{0, 500};
            case "3":
                return new long[]{0, 300, 250, 300};
            case "4":
                return new long[]{0, 400, 250, 400};
            case "5":
                return new long[]{0, 500, 250, 500};
            default:
                return new long[]{0, 400, 250, 400};
        }
    }

    @Override
    public boolean isQuickReplyImmediately() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("quick_reply_immediately", false);
    }

    @Override
    public void setDefault(int aid, int peerId){
        preferences.edit()
                .remove(keyFor(aid, peerId))
                .apply();
    }

    @Override
    public int getNotifPref(int aid, int peerid) {
        return preferences.getInt(keyFor(aid, peerid), getGlobalNotifPref(Peer.isGroupChat(peerid)));
    }

    private static String keyFor(int aid, int peerId){
        return "peerid" + aid + "_" + peerId;
    }

    private int getGlobalNotifPref(boolean isGroup){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app);
        int value = sharedPreferences.getBoolean("high_notif_priority", false) ? FLAG_HIGH_PRIORITY : 0;

        if (!isGroup){
            if (sharedPreferences.getBoolean("new_dialog_message_notif_enable", true)){
                value += FLAG_SHOW_NOTIF;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_sound", true)){
                value += FLAG_SOUND;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_vibration", true)){
                value += FLAG_VIBRO;
            }

            if (sharedPreferences.getBoolean("new_dialog_message_notif_led", true)){
                value += FLAG_LED;
            }
        } else {
            if (sharedPreferences.getBoolean("new_groupchat_message_notif_enable", true)){
                value += FLAG_SHOW_NOTIF;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_sound", true)){
                value += FLAG_SOUND;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_vibration", true)){
                value += FLAG_VIBRO;
            }

            if (sharedPreferences.getBoolean("new_groupchat_message_notif_led", true)) {
                value += FLAG_LED;
            }
        }
        return value;
    }
}

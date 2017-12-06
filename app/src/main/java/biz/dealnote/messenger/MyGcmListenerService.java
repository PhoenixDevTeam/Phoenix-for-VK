package biz.dealnote.messenger;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.gcm.GcmListenerService;

import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.push.CollapseKey;
import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.push.message.BirtdayGcmMessage;
import biz.dealnote.messenger.push.message.CommentGCMMessage;
import biz.dealnote.messenger.push.message.FriendAcceptedGCMMessage;
import biz.dealnote.messenger.push.message.FriendGCMMessage;
import biz.dealnote.messenger.push.message.GCMMessage;
import biz.dealnote.messenger.push.message.GroupInviteGCMMessage;
import biz.dealnote.messenger.push.message.LikeGcmMessage;
import biz.dealnote.messenger.push.message.NewPostPushMessage;
import biz.dealnote.messenger.push.message.ReplyGCMMessage;
import biz.dealnote.messenger.push.message.WallPostGCMMessage;
import biz.dealnote.messenger.push.message.WallPublishGCMMessage;
import biz.dealnote.messenger.realtime.Processors;
import biz.dealnote.messenger.realtime.QueueContainsException;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.PersistentLogger;

import static biz.dealnote.messenger.util.Utils.isEmpty;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param extras Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle extras) {
        Context context = getApplicationContext();
        String collapseKey = extras.getString("collapse_key");

        Logger.d(TAG, "onMessage, from: " + from + ", collapseKey: " + collapseKey + ", extras: " + extras);
        if (isEmpty(collapseKey)) {
            return;
        }

        StringBuilder bundleDump = new StringBuilder();
        for (String key : extras.keySet()) {
            try {
                Object value = extras.get(key);
                String line = "key: " + key + ", value: " + value + ", class: " + (value == null ? "null" : value.getClass());
                Logger.d(TAG, line);
                bundleDump.append("\n").append(line);
            } catch (Exception ignored) {
            }
        }

        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        if (!registrationResolver.canReceivePushNotification()) {
            Logger.d(TAG, "Invalid push registration on VK");
            return;
        }

        switch (collapseKey) {
            case CollapseKey.MSG:
                fireNewMessage(accountId, GCMMessage.genFromBundle(extras));
                break;
            case CollapseKey.WALL_POST:
                WallPostGCMMessage.fromBundle(extras).nofify(context, accountId);
                break;
            case CollapseKey.REPLY:
                ReplyGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.COMMENT:
                CommentGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.WALL_PUBLISH:
                WallPublishGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.FRIEND:
                FriendGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.FRIEND_ACCEPTED:
                FriendAcceptedGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.GROUP_INVITE:
                GroupInviteGCMMessage.fromBundle(extras).notify(context, accountId);
                break;
            case CollapseKey.BIRTHDAY:
                BirtdayGcmMessage.fromBundle(extras).notify(context, accountId);
                break;

            case CollapseKey.NEW_POST:
                new NewPostPushMessage(accountId, extras).notifyIfNeed(context);
                break;

            case CollapseKey.LIKE:
                new LikeGcmMessage(accountId, extras).notifyIfNeed(context);
                break;

            //case CollapseKey.BIRTHDAY:
            //    // TODO: 02.12.2016
            //    break;

            default:
                PersistentLogger.logThrowable("Push issues", new Exception("Unespected Push event, collapse_key: " + collapseKey + ", dump: " + bundleDump));
                break;
        }
    }

    private void fireNewMessage(int accountId, final @NonNull GCMMessage dto) {
        try {
            Processors.realtimeMessages().process(accountId, dto.getMessageId(), true);
        } catch (QueueContainsException ignored) {
        }

        if (dto.getBadge() >= 0) {
            Stores.getInstance()
                    .dialogs()
                    .setUnreadDialogsCount(accountId, dto.getBadge());
        }
    }
}
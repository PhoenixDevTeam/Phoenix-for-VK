package biz.dealnote.messenger.settings;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.model.SwitchableCategory;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.place.Place;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
public interface ISettings {

    IRecentChats recentChats();

    IDrawerSettings drawerSettings();

    IPushSettings pushSettings();

    ISecuritySettings security();

    IUISettings ui();

    INotificationSettings notifications();

    IMainSettings main();

    IAccountsSettings accounts();

    IOtherSettings other();

    interface IOtherSettings {
        String getFeedSourceIds(int accountId);

        void setFeedSourceIds(int accountId, String sourceIds);

        void storeFeedScrollState(int accountId, String state);

        String restoreFeedScrollState(int accountId);

        String restoreFeedNextFrom(int accountId);

        void storeFeedNextFrom(int accountId, String nextFrom);

        boolean isAudioBroadcastActive();

        void setAudioBroadcastActive(boolean active);

        boolean isForceExoplayer();

        boolean isKeepLongpoll();

        boolean isCommentsDesc();

        boolean toggleCommentsDirection();
    }

    interface IAccountsSettings {
        int INVALID_ID = -1;

        Flowable<Integer> observeChanges();

        Flowable<IAccountsSettings> observeRegistered();

        List<Integer> getRegistered();

        void setCurrent(int accountId);

        int getCurrent();

        void remove(int accountId);

        void registerAccountId(int accountId, boolean setCurrent);

        void storeAccessToken(int accountId, String accessToken);

        String getAccessToken(int accountId);

        void removeAccessToken(int accountId);
    }

    interface IMainSettings {

        void incrementRunCount();

        int getRunCount();

        void setRunCount(int count);

        boolean isSendByEnter();

        boolean isNeedDoublePressToExit();

        boolean isCustomTabEnabled();

        @Nullable
        Integer getUploadImageSize();

        @PhotoSize
        int getPrefPreviewImageSize();

        void notifyPrefPreviewSizeChanged();

        @PhotoSize
        int getPrefDisplayImageSize(@PhotoSize int byDefault);

        void setPrefDisplayImageSize(@PhotoSize int size);
    }

    interface INotificationSettings {
        int FLAG_SOUND = 1;
        int FLAG_VIBRO = 2;
        int FLAG_LED = 4;
        int FLAG_SHOW_NOTIF = 8;
        int FLAG_HIGH_PRIORITY = 16;

        int getNotifPref(int aid, int peerid);

        void setDefault(int aid, int peerId);

        void setNotifPref(int aid, int peerid, int flag);

        int getOtherNotificationMask();

        boolean isCommentsNotificationsEnabled();

        boolean isFriendRequestAcceptationNotifEnabled();

        boolean isNewFollowerNotifEnabled();

        boolean isWallPublishNotifEnabled();

        boolean isGroupInvitedNotifEnabled();

        boolean isReplyNotifEnabled();

        boolean isNewPostOnOwnWallNotifEnabled();

        boolean isNewPostsNotificationEnabled();

        boolean isLikeNotificationEnable();

        Uri getFeedbackRingtoneUri();

        String getDefNotificationRingtone();

        String getNotificationRingtone();

        void setNotificationRingtoneUri(String path);

        long[] getVibrationLength();

        boolean isQuickReplyImmediately();

        boolean isBirtdayNotifEnabled();
    }

    interface IRecentChats {
        List<RecentChat> get(int acountid);

        void store(int accountid, List<RecentChat> chats);
    }

    interface IDrawerSettings {
        boolean isCategoryEnabled(@SwitchableCategory int category);

        void setCategoriesOrder(@SwitchableCategory int[] order, boolean[] active);

        int[] getCategoriesOrder();

        Observable<Object> observeChanges();
    }

    interface IPushSettings {
        void savePushRegistations(Collection<VkPushRegistration> data);

        List<VkPushRegistration> getRegistrations();
    }

    interface ISecuritySettings {
        boolean isKeyEncryptionPolicyAccepted();

        void setKeyEncryptionPolicyAccepted(boolean accepted);

        boolean isPinValid(@NonNull int[] values);

        void setPin(@Nullable int[] pin);

        boolean isUsePinForEntrance();

        boolean isUsePinForSecurity();

        boolean isEntranceByFingerprintAllowed();

        @KeyLocationPolicy
        int getEncryptionLocationPolicy(int accountId, int peerId);

        void disableMessageEncryption(int accountId, int peerId);

        boolean isMessageEncryptionEnabled(int accountId, int peerId);

        void enableMessageEncryption(int accountId, int peerId, @KeyLocationPolicy int policy);

        void firePinAttemptNow();

        void clearPinHistory();

        List<Long> getPinEnterHistory();

        boolean hasPinHash();

        int getPinHistoryDepth();

        boolean needHideMessagesBodyForNotif();
    }

    interface IUISettings {
        @StyleRes
        int getMainTheme();

        @AvatarStyle
        int getAvatarStyle();

        void storeAvatarStyle(@AvatarStyle int style);

        boolean isDarkModeEnabled(Context context);

        int getNightMode();

        Place getDefaultPage(int accountId);

        void notifyPlaceResumed(int type);

        boolean isSystemEmoji();
    }
}
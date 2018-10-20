package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.LastReadId;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 03.10.2016.
 * phoenix
 */
public interface IBasicMessageListView extends IMvpView, IAttachmentsPlacesView, IAccountDependencyView, IToastView {
    void notifyMessagesUpAdded(int position, int count);
    void notifyDataChanged();
    void notifyMessagesDownAdded(int count);

    void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin);
    void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin);
    void disableVoicePlaying();

    void showActionMode(String title, Boolean canEdit, Boolean canPin);
    void finishActionMode();

    void displayMessages(@NonNull List<Message> mData, @NonNull LastReadId lastReadId);
}
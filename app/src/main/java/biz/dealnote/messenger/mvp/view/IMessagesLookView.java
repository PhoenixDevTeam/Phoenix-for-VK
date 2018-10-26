package biz.dealnote.messenger.mvp.view;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public interface IMessagesLookView extends IBasicMessageListView, IErrorView {

    void focusTo(int index);
    void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState);

    void forwardMessages(int accountId, @NonNull ArrayList<Message> messages);
}

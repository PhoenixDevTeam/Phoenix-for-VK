package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.view.IMessagesLookView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Observable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.getSelected;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by ruslan.kolbasa on 03.10.2016.
 * phoenix
 */
public class MessagesLookPresenter extends AbsMessageListPresenter<IMessagesLookView> {

    private static final int COUNT = 40;

    private static final String TAG = MessagesLookPresenter.class.getSimpleName();

    private int mPeerId;
    private Integer mFocusMessageId;

    private final IMessagesInteractor messagesInteractor;

    public MessagesLookPresenter(int accountId, int peerId, Integer focusTo, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.messagesInteractor = InteractorFactory.createMessagesInteractor();
        mPeerId = peerId;

        if (savedInstanceState == null) {
            mFocusMessageId = focusTo;
            initRequest();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IMessagesLookView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayMessages(getData());
        resolveHeaders();
    }

    private int loadingState;

    private void initRequest() {
        if (isLoadingNow()) return;

        this.loadingState = Side.INIT;
        resolveHeaders();

        final int accountId = super.getAccountId();

        appendDisposable(messagesInteractor.getPeerMessages(accountId, mPeerId, COUNT, -COUNT / 2, mFocusMessageId, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        this.loadingState = Side.NO_LOADING;
        resolveHeaders();

        showError(getView(), getCauseIfRuntime(t));
    }

    private void onDataReceived(List<Message> messages) {
        switch (loadingState) {
            case Side.INIT:
                onInitDataLoaded(messages);
                break;
            case Side.UP:
                onUpDataLoaded(messages);
                break;
            case Side.DOWN:
                onDownDataLoaded(messages);
                break;
        }

        this.loadingState = Side.NO_LOADING;
        resolveHeaders();
    }

    private boolean isLoadingNow() {
        return loadingState != Side.NO_LOADING;
    }

    public void fireFooterLoadMoreClick() {
        loadMoreDown();
    }

    public void fireHeaderLoadMoreClick() {
        loadMoreUp();
    }

    private void loadMoreDown() {
        if (isLoadingNow()) return;

        Integer firstMessageId = getFirstMessageId();
        if (firstMessageId == null) {
            return;
        }

        this.loadingState = Side.DOWN;
        resolveHeaders();

        final int accountId = super.getAccountId();

        int targetMessageId = firstMessageId + 1; //чтобы не зацепить уже загруженное сообщение

        appendDisposable(messagesInteractor.getPeerMessages(accountId, mPeerId, COUNT, -COUNT, targetMessageId, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    @Override
    protected void onActionModeDeleteClick() {
        super.onActionModeDeleteClick();
        final int accountId = super.getAccountId();

        List<Integer> ids = Observable.fromIterable(getData())
                .filter(Message::isSelected)
                .map(Message::getId)
                .toList()
                .blockingGet();

        if (nonEmpty(ids)) {
            appendDisposable(messagesInteractor.deleteMessages(accountId, ids)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> onMessagesDeleteSuccessfully(ids), t -> showError(getView(), getCauseIfRuntime(t))));
        }
    }

    private void loadMoreUp() {
        if (isLoadingNow()) return;

        Integer lastMessageId = getLastMessageId();
        if (lastMessageId == null) {
            return;
        }

        this.loadingState = Side.UP;
        resolveHeaders();

        final int targetLastMessageId = lastMessageId - 1; //чтобы не зацепить уже загруженное сообщение
        final int accountId = super.getAccountId();

        appendDisposable(messagesInteractor.getPeerMessages(accountId, mPeerId, COUNT, 0, targetLastMessageId, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private Integer getLastMessageId() {
        return isEmpty(getData()) ? null : getData().get(getData().size() - 1).getId();
    }

    private Integer getFirstMessageId() {
        return isEmpty(getData()) ? null : getData().get(0).getId();
    }

    @Override
    protected void onActionModeForwardClick() {
        super.onActionModeForwardClick();
        ArrayList<Message> selected = getSelected(getData());

        if (nonEmpty(selected)) {
            getView().forwardMessages(getAccountId(), selected);
        }
    }

    @SuppressWarnings("unused")
    public void fireMessageRestoreClick(@NonNull Message message, int position) {
        final int accountId = super.getAccountId();
        final int id = message.getId();

        appendDisposable(messagesInteractor.restoreMessage(accountId, id)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onMessageRestoredSuccessfully(id), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private static class Side {
        static final int NO_LOADING = 0;
        static final int INIT = 1;
        static final int UP = 2;
        static final int DOWN = 3;
    }

    private void resolveHeaders() {
        if (!isGuiReady()) return;

        @LoadMoreState
        int headerState = LoadMoreState.INVISIBLE;

        @LoadMoreState
        int footerState = LoadMoreState.INVISIBLE;

        switch (loadingState) {
            case Side.UP:
                headerState = LoadMoreState.LOADING;
                footerState = LoadMoreState.INVISIBLE;
                break;

            case Side.INIT:
                headerState = LoadMoreState.INVISIBLE;
                footerState = LoadMoreState.INVISIBLE;
                break;

            case Side.DOWN:
                headerState = LoadMoreState.INVISIBLE;
                footerState = LoadMoreState.LOADING;
                break;

            case Side.NO_LOADING:
                headerState = LoadMoreState.CAN_LOAD_MORE;
                footerState = LoadMoreState.CAN_LOAD_MORE;
                break;
        }

        getView().setupHeaders(headerState, footerState);
    }

    private void onMessageRestoredSuccessfully(int id) {
        Message message = findById(id);

        if (Objects.nonNull(message)) {
            message.setDeleted(false);
            safeNotifyDataChanged();
        }
    }

    private void onMessagesDeleteSuccessfully(Collection<Integer> ids) {
        for (Integer id : ids) {
            Message message = findById(id);

            if (Objects.nonNull(message)) {
                message.setDeleted(true);
            }
        }

        safeNotifyDataChanged();
    }

    private void onInitDataLoaded(List<Message> messages) {
        super.getData().clear();
        super.getData().addAll(messages);

        if (isGuiReady()) {
            getView().notifyDataChanged();
        }

        if (mFocusMessageId != null) {
            int index = Utils.indexOf(messages, mFocusMessageId);

            if (isGuiReady()) {
                mFocusMessageId = null;

                if (index != -1) {
                    getView().focusTo(index);
                }
            }
        }
    }

    private void onUpDataLoaded(List<Message> messages) {
        int size = getData().size();

        super.getData().addAll(messages);

        if (isGuiReady()) {
            getView().notifyMessagesUpAdded(size, messages.size());
        }
    }

    private void onDownDataLoaded(List<Message> messages) {
        super.getData().addAll(0, messages);

        if (isGuiReady()) {
            getView().notifyMessagesDownAdded(messages.size());
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
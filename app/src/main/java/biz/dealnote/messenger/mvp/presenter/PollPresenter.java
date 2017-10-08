package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IPollInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IPollView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public class PollPresenter extends AccountDependencyPresenter<IPollView> {

    private static final String TAG = PollPresenter.class.getSimpleName();

    private Poll mPoll;
    private int mTempCheckedId;

    private final IPollInteractor pollInteractor;

    public PollPresenter(int accountId, @NonNull Poll poll, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mPoll = poll;
        this.pollInteractor = InteractorFactory.createPollInteractor();

        refreshPollData();
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveButtonView();
    }

    private void refreshPollData() {
        if (loadingNow) return;

        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(pollInteractor.getPollById(accountId, mPoll.getOwnerId(), mPoll.getId(), mPoll.isBoard())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private void onLoadingError(Throwable t) {
        showError(getView(), Utils.getCauseIfRuntime(t));
        setLoadingNow(false);
    }

    private void onPollInfoUpdated(Poll poll) {
        setLoadingNow(false);

        mPoll = poll;

        if (mPoll.getMyAnswerId() != 0) {
            mTempCheckedId = 0;
        }

        resolveQuestionView();
        resolveVotesCountView();
        resolvePollTypeView();
        resolveVotesListView();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnGuiCreated
    private void resolveButtonView() {
        if (isGuiReady()) {
            getView().displayLoading(loadingNow);
            getView().setupButton(mPoll.getMyAnswerId() != 0);
        }
    }

    @OnGuiCreated
    private void resolveVotesListView() {
        if (isGuiReady()) {
            getView().displayVotesList(mPoll.getAnswers(), mPoll.getMyAnswerId() == 0, mTempCheckedId);
        }
    }

    @OnGuiCreated
    private void resolveVotesCountView() {
        if (isGuiReady()) {
            getView().displayVoteCount(mPoll.getVoteCount());
        }
    }

    @OnGuiCreated
    private void resolvePollTypeView() {
        if (isGuiReady()) {
            getView().displayType(mPoll.isAnonymous());
        }
    }

    @OnGuiCreated
    private void resolveQuestionView() {
        if (isGuiReady()) {
            getView().displayQuestion(mPoll.getQuestion());
        }
    }

    @OnGuiCreated
    private void resolveCreationTimeView() {
        if (isGuiReady()) {
            getView().displayCreationTime(mPoll.getCreationTime());
        }
    }

    public void fireVoteChecked(int newid) {
        mTempCheckedId = newid;
    }

    private void remove() {
        if (loadingNow) return;

        final int accountId = super.getAccountId();
        final int answerId = this.mPoll.getMyAnswerId();

        setLoadingNow(true);
        appendDisposable(pollInteractor.removeVote(accountId, this.mPoll, answerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private void vote() {
        if (loadingNow) return;

        final int accountId = super.getAccountId();
        final int voteId = this.mTempCheckedId;

        setLoadingNow(true);
        appendDisposable(pollInteractor.addVote(accountId, this.mPoll, voteId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    public void fireButtonClick() {
        if (loadingNow) return;

        if (mPoll.getMyAnswerId() == 0) {
            if (mTempCheckedId == 0) {
                getView().showError(R.string.select);
                return;
            }

            vote();
        } else {
            remove();
        }
    }
}
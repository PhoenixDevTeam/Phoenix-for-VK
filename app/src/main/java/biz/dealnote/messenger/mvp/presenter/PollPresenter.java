package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IPollView;
import biz.dealnote.messenger.service.RequestFactory;
import biz.dealnote.messenger.service.factory.PollRequestFactory;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public class PollPresenter extends AccountDependencyPresenter<IPollView> {

    private static final String TAG = PollPresenter.class.getSimpleName();

    private Poll mPoll;
    private int mTempCheckedId;

    public PollPresenter(int accountId, @NonNull Poll poll, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mPoll = poll;

        refreshPollData();
    }

    private void refreshPollData(){
        Request request = PollRequestFactory.getGetPollById(mPoll.getId(), mPoll.getOwnerId(), mPoll.isBoard());
        executeRequest(request);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnGuiCreated
    private void resolveButtonView() {
        if (isGuiReady()) {
            getView().displayLoading(isLoadingNow());
            getView().setupButton(mPoll.getMyAnswerId() != 0);
        }
    }

    private boolean isLoadingNow() {
        return hasRequest(RequestFactory.REQUEST_ADD_VOTE, RequestFactory.REQUEST_REMOVE_VOTE,
                PollRequestFactory.REQUEST_GET_POLL_BY_ID);
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

    @Override
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {
        super.onRequestFinished(request, resultData);

        switch (request.getRequestType()){
            case RequestFactory.REQUEST_ADD_VOTE:
            case RequestFactory.REQUEST_REMOVE_VOTE:
            case PollRequestFactory.REQUEST_GET_POLL_BY_ID:
                Poll result = resultData.getParcelable(Extra.POLL);
                if(nonNull(result)){
                    onPollUpdated(result);
                }
                break;
        }

        resolveButtonView();
    }

    @Override
    protected void onRequestError(@NonNull Request request, @NonNull ServiceException e) {
        super.onRequestError(request, e);
        safeShowError(getView(), e.getMessage());
        resolveButtonView();
    }

    private void onPollUpdated(@NonNull Poll poll) {
        Logger.d(TAG, "onPollUpdated, poll: " + poll);

        mPoll = poll;

        if(mPoll.getMyAnswerId() != 0){
            mTempCheckedId = 0;
        }

        resolveQuestionView();
        resolveVotesCountView();
        resolvePollTypeView();
        resolveVotesListView();
    }

    private void remove() {
        Request request = RequestFactory.getRemoveVoteRequest(mPoll, mPoll.getMyAnswerId());
        executeRequest(request);
        resolveButtonView();
    }

    private void vote() {
        Request request = RequestFactory.getAddVoteRequest(mPoll, mTempCheckedId);
        executeRequest(request);
        resolveButtonView();
    }

    public void fireButtonClick() {
        if (isLoadingNow()) return;

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

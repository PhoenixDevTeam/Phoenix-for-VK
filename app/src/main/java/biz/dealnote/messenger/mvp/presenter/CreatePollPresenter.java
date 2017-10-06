package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICreatePollView;
import biz.dealnote.messenger.service.factory.PollRequestFactory;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

/**
 * Created by admin on 20.12.2016.
 * phoenix
 */
public class CreatePollPresenter extends AccountDependencyPresenter<ICreatePollView> {

    private static final String TAG = CreatePollPresenter.class.getSimpleName();

    private String mQuestion;
    private String[] mOptions;
    private int mOwnerId;
    private boolean mAnonymous;

    public CreatePollPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mOwnerId = ownerId;

        if (isNull(savedInstanceState)) {
            mOptions = new String[10];
        }
    }

    @Override
    public void onGuiCreated(@NonNull ICreatePollView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayQuestion(mQuestion);
        viewHost.setAnonymous(mAnonymous);
        viewHost.displayOptions(mOptions);
    }

    @Override
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {
        super.onRequestFinished(request, resultData);
        if(request.getRequestType() == PollRequestFactory.REQUEST_POLL_CREATE){
            Poll poll = resultData.getParcelable(Extra.POLL);
            AssertUtils.requireNonNull(poll);

            resolveProgressDialog();

            if(isGuiReady()){
                getView().sendResultAndGoBack(poll);
            }
        }
    }

    @Override
    protected void onRequestError(@NonNull Request request, @NonNull ServiceException e) {
        super.onRequestError(request, e);
        safeShowError(getView(), e.getMessage());
        resolveProgressDialog();
    }

    private void create() {
        if (safeIsEmpty(mQuestion)) {
            getView().showQuestionError(R.string.field_is_required);
            return;
        }

        List<String> nonEmptyOptions = new ArrayList<>();
        for (String o : mOptions) {
            if (!safeIsEmpty(o)) {
                nonEmptyOptions.add("\"" + o + "\"");
            }
        }

        if (nonEmptyOptions.isEmpty()) {
            getView().showOptionError(0, R.string.field_is_required);
            return;
        }

        Request request = PollRequestFactory.getCreatePollRequest(mQuestion, mAnonymous, mOwnerId, nonEmptyOptions);
        executeRequest(request);

        resolveProgressDialog();
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (hasRequest(PollRequestFactory.REQUEST_POLL_CREATE)) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireQuestionEdited(CharSequence text) {
        mQuestion = isNull(text) ? null : text.toString();
    }

    public void fireOptionEdited(int index, CharSequence s) {
        mOptions[index] = isNull(s) ? null : s.toString();
    }

    public void fireAnonyamousChecked(boolean b) {
        mAnonymous = b;
    }

    public void fireDoneClick() {
        create();
    }
}

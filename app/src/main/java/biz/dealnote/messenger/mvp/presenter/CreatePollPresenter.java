package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IPollInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICreatePollView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
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

    private final IPollInteractor pollInteractor;

    public CreatePollPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.mOwnerId = ownerId;
        this.pollInteractor = InteractorFactory.createPollInteractor();

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

    private boolean creationNow;

    private void setCreationNow(boolean creationNow) {
        this.creationNow = creationNow;
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

        setCreationNow(true);
        final int accountId = super.getAccountId();

        appendDisposable(pollInteractor.createPoll(accountId, mQuestion, mAnonymous, mOwnerId, nonEmptyOptions)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollCreated, this::onPollCreateError));
    }

    private void onPollCreateError(Throwable t) {
        setCreationNow(false);
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onPollCreated(Poll poll) {
        setCreationNow(false);
        callView(view -> view.sendResultAndGoBack(poll));
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (creationNow)
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
        } else {
            getView().dismissProgressDialog();
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
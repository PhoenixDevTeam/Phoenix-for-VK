package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 20.12.2016.
 * phoenix
 */
public interface ICreatePollView extends IAccountDependencyView, IMvpView, IProgressView, IErrorView {
    void displayQuestion(String question);

    void setAnonymous(boolean anomymous);

    void displayOptions(String[] options);

    void showQuestionError(@StringRes int message);

    void showOptionError(int index, @StringRes int message);

    void sendResultAndGoBack(@NonNull Poll poll);
}

package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.feedback.Feedback;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 11.12.2016.
 * phoenix
 */
public interface IFeedbackView extends IAccountDependencyView, IMvpView, IAttachmentsPlacesView, IErrorView {
    void displayData(List<Feedback> data);
    void showLoading(boolean loading);
    void notifyDataAdding(int position, int count);
    void notifyDataSetChanged();
    void configLoadMore(@LoadMoreState int loadmoreState);
    void showLinksDialog(int accountId, @NonNull Feedback notification);
}

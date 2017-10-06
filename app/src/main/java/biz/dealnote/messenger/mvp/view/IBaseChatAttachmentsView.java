package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public interface IBaseChatAttachmentsView<T> extends IMvpView, IAccountDependencyView,
        IAttachmentsPlacesView, IErrorView {

    void displayAttachments(List<T> data);

    void notifyDataAdded(int position, int count);

    void notifyDatasetChanged();

    void showLoading(boolean loading);

    void setEmptyTextVisible(boolean visible);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);
}

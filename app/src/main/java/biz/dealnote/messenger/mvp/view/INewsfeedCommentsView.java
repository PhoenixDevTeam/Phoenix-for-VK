package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.NewsfeedComment;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public interface INewsfeedCommentsView extends IAccountDependencyView, IAttachmentsPlacesView, IMvpView, IErrorView {
    void displayData(List<NewsfeedComment> data);
    void notifyDataAdded(int position, int count);
    void notifyDataSetChanged();
    void showLoading(boolean loading);
}

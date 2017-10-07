package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IFaveLinksView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayLinks(List<FaveLink> links);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void displayRefreshing(boolean refreshing);
    void openLink(int accountId, FaveLink link);
    void notifyItemRemoved(int index);
}
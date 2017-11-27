package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.DataWrapper;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 19.09.2017.
 * phoenix
 */
public interface ICommunitiesView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> seacrh);
    void notifyDataSetChanged();
    void notifyOwnDataAdded(int position, int count);
    void displayRefreshing(boolean refreshing);

    void showCommunityWall(int accountId, Community community);

    void notifySeacrhDataAdded(int position, int count);
}
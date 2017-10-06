package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public interface IFriendsTabsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayConters(FriendsCounters counters);
    void configTabs(int accountId, int userId, boolean showMutualTab);
    void displayUserNameAtToolbar(String userName);

    void setDrawerFriendsSectionSelected(boolean selected);
}
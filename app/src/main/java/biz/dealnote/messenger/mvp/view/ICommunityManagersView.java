package biz.dealnote.messenger.mvp.view;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public interface ICommunityManagersView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void notifyDataSetChanged();

    void displayRefreshing(boolean loadingNow);

    void displayData(List<Manager> managers);

    void goToManagerEditing(int accountId, int groupId, Manager manager);

    void showUserProfile(int accountId, User user);

    void startSelectProfilesActivity(int accountId, int groupId);

    void startAddingUsersToManagers(int accountId, int groupId, ArrayList<User> users);

    void notifyItemRemoved(int index);

    void notifyItemChanged(int index);

    void notifyItemAdded(int index);
}

package biz.dealnote.messenger.mvp.view;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public interface ICommunityBlacklistView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void diplayData(List<Banned> data);

    void notifyItemRemoved(int index);

    void openBanEditor(int accountId, int groupId, Banned banned);

    void startSelectProfilesActivity(int accountId, int groupId);

    void addUsersToBan(int accountId, int groupId, ArrayList<User> users);

    void notifyItemsAdded(int position, int size);
}

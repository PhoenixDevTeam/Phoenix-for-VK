package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public interface IUserBannedView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayUserList(List<User> users);

    void notifyItemsAdded(int position, int count);
    void notifyDataSetChanged();
    void notifyItemRemoved(int position);

    void displayRefreshing(boolean refreshing);

    void startUserSelection(int accountId);
    void showSuccessToast();

    void scrollToPosition(int position);

    void showUserProfile(int accountId, User user);
}
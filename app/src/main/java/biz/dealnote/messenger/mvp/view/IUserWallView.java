package biz.dealnote.messenger.mvp.view;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.model.PostFilter;
import biz.dealnote.messenger.model.User;

/**
 * Created by ruslan.kolbasa on 23.01.2017.
 * phoenix
 */
public interface IUserWallView extends IWallView, IProgressView, ISnackbarView {

    void displayWallFilters(List<PostFilter> filters);
    void notifyWallFiltersChanged();

    void setupPrimaryActionButton(@StringRes Integer title);

    void openFriends(int accountId, int userId, int tab, FriendsCounters counters);

    void openGroups(int accountId, int userId, @Nullable User user);

    void showEditStatusDialog(String initialValue);

    void showAddToFriendsMessageDialog();

    void showAvatarContextMenu();

    void displayCounters(int friends, int followers, int groups, int photos, int audios, int videos);

    void displayUserStatus(String statusText);

    void displayBaseUserInfo(User user);
}
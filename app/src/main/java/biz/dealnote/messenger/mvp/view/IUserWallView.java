package biz.dealnote.messenger.mvp.view;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.PostFilter;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserDetails;

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

    void showAvatarContextMenu(boolean canUploadAvatar);

    void displayCounters(int friends, int followers, int groups, int photos, int audios, int videos);

    void displayUserStatus(String statusText);

    void displayBaseUserInfo(User user);

    void openUserDetails(int accountId, @NonNull User user, @NonNull UserDetails details);

    void showAvatarUploadedMessage(int accountId, Post post);
}
package biz.dealnote.messenger.mvp.view;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.PostFilter;

/**
 * Created by admin on 23.01.2017.
 * phoenix
 */
public interface IGroupWallView extends IWallView {

    void displayBaseCommunityData(Community community);
    void displayCommunityCover(boolean enabled, String resource);

    void setupPrimaryButton(@StringRes Integer title);
    void setupSecondaryButton(@StringRes Integer title);

    void openTopics(int accoundId, int ownerId, @Nullable Owner owner);
    void openCommunityMembers(int accoundId, int groupId);
    void openDocuments(int accoundId, int ownerId, @Nullable Owner owner);

    void displayWallFilters(List<PostFilter> filters);
    void notifyWallFiltersChanged();

    void goToCommunityControl(int accountId, Community community, GroupSettings settings);

    void startLoginCommunityActivity(int groupId);

    void openCommunityDialogs(int accountId, int groupId, String subtitle);

    void displayCounters(int members, int topics, int docs, int photos, int audio, int video);

    interface IOptionMenuView {
        void setControlVisible(boolean visible);
    }
}
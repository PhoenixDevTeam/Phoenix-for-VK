package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public interface IWallPostView extends IAttachmentsPlacesView, IAccountDependencyView, IMvpView, IErrorView {

    void displayDefaultToolbaTitle();
    void displayToolbarTitle(String title);

    void displayToolbatSubtitle(int subtitleType, long datetime);

    int SUBTITLE_NORMAL = 1;
    int SUBTITLE_STATUS_UPDATE = 2;
    int SUBTITLE_PHOTO_UPDATE = 3;

    void displayPostInfo(Post post);
    void displayLoading();
    void displayLoadingFail();

    void displayLikes(int count, boolean userLikes);
    void setCommentButtonVisible(boolean visible);
    void displayCommentCount(int count);
    void displayReposts(int count, boolean userReposted);

    void goToPostEditing(int accountId, @NonNull Post post);

    void showPostNotReadyToast();

    void copyLinkToClipboard(String link);

    void copyTextToClipboard(String text);

    void displayDefaultToolbaSubitle();

    void displayPinComplete(boolean pin);

    void displayDeleteOrRestoreComplete(boolean deleted);

    void goToNewsSearch(int accountId, String hashTag);

    interface IOptionView {
        void setCanDelete(boolean can);
        void setCanRestore(boolean can);
        void setCanPin(boolean can);
        void setCanUnpin(boolean can);
        void setCanEdit(boolean can);
    }
}
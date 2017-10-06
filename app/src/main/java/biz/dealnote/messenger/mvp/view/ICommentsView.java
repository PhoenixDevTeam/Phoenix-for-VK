package biz.dealnote.messenger.mvp.view;

import android.support.annotation.StringRes;

import java.util.List;

import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 07.06.2017.
 * phoenix
 */
public interface ICommentsView extends IAccountDependencyView, IAttachmentsPlacesView, IMvpView,
        IErrorView, IToolbarView, IProgressView, IToastView {

    void displayData(List<Comment> data);

    void notifyDataSetChanged();

    void setupLoadUpHeader(@LoadMoreState int state);

    void setupLoadDownFooter(@LoadMoreState int state);

    void notifyDataAddedToTop(int count);

    void notifyDataAddedToBottom(int count);

    void notifyItemChanged(int index);

    void moveFocusTo(int index, boolean smooth);

    void displayBody(String body);

    void displayAttachmentsCount(int count);

    void setButtonSendAvailable(boolean available);

    void openAttachmentsManager(int accountId, Integer draftCommentId, int sourceOwnerId, String draftCommentBody);

    void setupReplyViews(String replyTo);

    void replaceBodySelectionTextTo(String replyText);

    void goToCommentEdit(int accountId, Comment comment);

    void goToWallPost(int accountId, int postId, int postOwnerId);

    void goToVideoPreview(int accountId, int videoId, int videoOwnerId);

    void banUser(int accountId, int groupId, User user);

    void displayAuthorAvatar(String url);

    void showAuthorSelectDialog(List<Owner> owners);

    interface ICommentContextView {
        void setCanEdit(boolean can);
        void setCanDelete(boolean can);
        void setCanBan(boolean can);
    }

    void setupOptionMenu(boolean topicPollAvailable, boolean gotoSourceAvailable, @StringRes Integer gotoSourceText);

    void setEpmtyTextVisible(boolean visible);

    void setCenterProgressVisible(boolean visible);

    void displayDeepLookingCommentProgress();

    void dismissDeepLookingCommentProgress();

    void setCanSendSelectAuthor(boolean can);
}
package biz.dealnote.messenger.adapter.feedback;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.AttachmentsViewBinder;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.model.feedback.CommentFeedback;
import biz.dealnote.messenger.model.feedback.CopyFeedback;
import biz.dealnote.messenger.model.feedback.Feedback;
import biz.dealnote.messenger.model.feedback.FeedbackType;
import biz.dealnote.messenger.model.feedback.LikeCommentFeedback;
import biz.dealnote.messenger.model.feedback.LikeFeedback;
import biz.dealnote.messenger.model.feedback.MentionCommentFeedback;
import biz.dealnote.messenger.model.feedback.MentionFeedback;
import biz.dealnote.messenger.model.feedback.PostPublishFeedback;
import biz.dealnote.messenger.model.feedback.ReplyCommentFeedback;
import biz.dealnote.messenger.model.feedback.UsersFeedback;
import biz.dealnote.messenger.view.OnlineView;

public class FeedbackAdapter extends RecyclerBindableAdapter<Feedback, FeedbackAdapter.FeedbackHolder> {

    private static final int TYPE_COMMENTS = 0;
    private static final int TYPE_USERS = 1;

    private static final int HEADER_DISABLE = 0;
    private static final int HEADER_TODAY = 1;
    private static final int HEADER_THIS_WEEK = 2;
    private static final int HEADER_OLD = 3;
    private static final int HEADER_YESTERDAY = 4;

    private Context mContext;
    private FeedbackViewBinder mFeedbackViewBinder;

    public FeedbackAdapter(Activity context, List<Feedback> items, AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        super(items);
        this.mContext = context;
        this.mFeedbackViewBinder = new FeedbackViewBinder(context, attachmentsActionCallback);
    }

    @Override
    protected void onBindItemViewHolder(FeedbackHolder viewHolder, int position, int type) {
        final Feedback item = getItem(position);
        Feedback previous = position > 0 ? getItem(position - 1) : null;

        int headerStatus = getHeaderStatus(previous, item.getDate());

        switch (headerStatus) {
            case HEADER_DISABLE:
                viewHolder.headerRoot.setVisibility(View.GONE);
                break;
            case HEADER_OLD:
                viewHolder.headerRoot.setVisibility(View.VISIBLE);
                viewHolder.headerText.setText(mContext.getString(R.string.dialog_day_older));
                break;
            case HEADER_TODAY:
                viewHolder.headerRoot.setVisibility(View.VISIBLE);
                viewHolder.headerText.setText(mContext.getString(R.string.dialog_day_today));
                break;
            case HEADER_YESTERDAY:
                viewHolder.headerRoot.setVisibility(View.VISIBLE);
                viewHolder.headerText.setText(mContext.getString(R.string.dialog_day_yesterday));
                break;
            case HEADER_THIS_WEEK:
                viewHolder.headerRoot.setVisibility(View.VISIBLE);
                viewHolder.headerText.setText(mContext.getString(R.string.dialog_day_ten_days));
                break;
        }

        if (viewHolder instanceof CommentHolder) {
            configCommentHolder(item, (CommentHolder) viewHolder);
        }

        if (viewHolder instanceof UsersHolder) {
            configUserHolder(item, (UsersHolder) viewHolder);
        }

        viewHolder.contentRoot.setOnClickListener(v -> {
            if(mClickListener != null){
                mClickListener.onNotificationClick(item);
            }
        });
    }

    private ClickListener mClickListener;

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener extends EventListener {
        void onNotificationClick(Feedback notification);
    }

    private void configCommentHolder(Feedback notification, CommentHolder holder) {
        switch (notification.getType()) {
            case FeedbackType.WALL_PUBLISH:
                mFeedbackViewBinder.configWallPublishFeedback((PostPublishFeedback) notification, holder);
                break;
            case FeedbackType.WALL:
                mFeedbackViewBinder.configWallFeedback((PostPublishFeedback) notification, holder);
                break;
            case FeedbackType.MENTION:
                mFeedbackViewBinder.configMentionFeedback((MentionFeedback) notification, holder);
                break;
            case FeedbackType.REPLY_COMMENT:
                mFeedbackViewBinder.configReplyCommentFeedback((ReplyCommentFeedback) notification, holder);
                break;
            case FeedbackType.REPLY_TOPIC:
                mFeedbackViewBinder.configReplyTopicFeedback((ReplyCommentFeedback) notification, holder);
                break;
            case FeedbackType.REPLY_COMMENT_PHOTO:
                mFeedbackViewBinder.configReplyCommentPhotoFeedback((ReplyCommentFeedback) notification, holder);
                break;
            case FeedbackType.REPLY_COMMENT_VIDEO:
                mFeedbackViewBinder.configReplyCommentVideoFeedback((ReplyCommentFeedback) notification, holder);
                break;
            case FeedbackType.MENTION_COMMENT_POST:
                mFeedbackViewBinder.configMentionCommentsFeedback((MentionCommentFeedback) notification, holder);
                break;
            case FeedbackType.MENTION_COMMENT_PHOTO:
                mFeedbackViewBinder.configMentionCommentsPhotoFeedback((MentionCommentFeedback) notification, holder);
                break;
            case FeedbackType.MENTION_COMMENT_VIDEO:
                mFeedbackViewBinder.configMentionCommentsVideoFeedback((MentionCommentFeedback) notification, holder);
                break;
            case FeedbackType.COMMENT_POST:
                mFeedbackViewBinder.configCommentPostFeedback((CommentFeedback) notification, holder);
                break;
            case FeedbackType.COMMENT_PHOTO:
                mFeedbackViewBinder.configCommentPhotoFeedback((CommentFeedback) notification, holder);
                break;
            case FeedbackType.COMMENT_VIDEO:
                mFeedbackViewBinder.configCommentVideoFeedback((CommentFeedback) notification, holder);
                break;
        }
    }

    private void configUserHolder(Feedback notification, UsersHolder holder) {
        switch (notification.getType()) {
            case FeedbackType.LIKE_POST:
                mFeedbackViewBinder.configLikePostFeedback((LikeFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_PHOTO:
                mFeedbackViewBinder.configLikePhotoFeedback((LikeFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_VIDEO:
                mFeedbackViewBinder.configLikeVideoFeedback((LikeFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_COMMENT_POST:
                mFeedbackViewBinder.configLikeCommentFeedback((LikeCommentFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_COMMENT_TOPIC:
                mFeedbackViewBinder.configLikeCommentTopicFeedback((LikeCommentFeedback) notification, holder);
                break;
            case FeedbackType.FOLLOW:
                mFeedbackViewBinder.configFollowFeedback((UsersFeedback) notification, holder);
                break;
            case FeedbackType.FRIEND_ACCEPTED:
                mFeedbackViewBinder.configFriendAcceptedFeedback((UsersFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_COMMENT_PHOTO:
                mFeedbackViewBinder.configLikeCommentForPhotoFeedback((LikeCommentFeedback) notification, holder);
                break;
            case FeedbackType.LIKE_COMMENT_VIDEO:
                mFeedbackViewBinder.configLikeCommentVideoFeedback((LikeCommentFeedback) notification, holder);
                break;
            case FeedbackType.COPY_POST:
                mFeedbackViewBinder.configCopyPostFeedback((CopyFeedback) notification, holder);
                break;
            case FeedbackType.COPY_PHOTO:
                mFeedbackViewBinder.configCopyPhotoFeedback((CopyFeedback) notification, holder);
                break;
            case FeedbackType.COPY_VIDEO:
                mFeedbackViewBinder.configCopyVideoFeedback((CopyFeedback) notification, holder);
                break;
        }
    }

    private int getItemViewType(Feedback notification) {
        switch (notification.getType()) {
            case FeedbackType.WALL:
            case FeedbackType.WALL_PUBLISH:
            case FeedbackType.MENTION:
            case FeedbackType.COMMENT_POST:
            case FeedbackType.MENTION_COMMENT_POST:
            case FeedbackType.COMMENT_PHOTO:
            case FeedbackType.MENTION_COMMENT_VIDEO:
            case FeedbackType.MENTION_COMMENT_PHOTO:
            case FeedbackType.COMMENT_VIDEO:
            case FeedbackType.REPLY_COMMENT:
            case FeedbackType.REPLY_COMMENT_PHOTO:
            case FeedbackType.REPLY_COMMENT_VIDEO:
            case FeedbackType.REPLY_TOPIC:
                return TYPE_COMMENTS;
            case FeedbackType.FOLLOW:
            case FeedbackType.FRIEND_ACCEPTED:
            case FeedbackType.COPY_POST:
            case FeedbackType.COPY_PHOTO:
            case FeedbackType.COPY_VIDEO:
            case FeedbackType.LIKE_POST:
            case FeedbackType.LIKE_PHOTO:
            case FeedbackType.LIKE_VIDEO:
            case FeedbackType.LIKE_COMMENT_POST:
            case FeedbackType.LIKE_COMMENT_PHOTO:
            case FeedbackType.LIKE_COMMENT_VIDEO:
            case FeedbackType.LIKE_COMMENT_TOPIC:
                return TYPE_USERS;
        }

        throw new IllegalArgumentException("Invalid feedback type: " + notification.getType());
    }

    @Override
    protected int getItemType(int position) {
        Feedback notification = getItem(position - getHeadersCount());
        return getItemViewType(notification);
    }

    @Override
    protected FeedbackHolder viewHolder(View view, int type) {
        switch (type) {
            case TYPE_COMMENTS:
                return new CommentHolder(view);
            case TYPE_USERS:
                return new UsersHolder(view);
        }

        return null;
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_COMMENTS:
                return R.layout.item_feedback_comment;
            case TYPE_USERS:
                return R.layout.item_feedback_user;
        }

        return 0;
    }

    private int getHeaderStatus(Feedback previous, long date) {
        Long previousDate = null;
        if (previous != null) {
            previousDate = previous.getDate() * 1000;
        }
        int stCurrent = getStatus(date * 1000);
        if (previousDate == null) {
            return stCurrent;
        } else {
            int stPrevious = getStatus(previousDate);
            if (stCurrent == stPrevious) {
                return HEADER_DISABLE;
            } else {
                return stCurrent;
            }
        }
    }

    private int getStatus(long time) {
        Calendar current = Calendar.getInstance();
        current.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DATE), 0, 0, 0);
        long today = current.getTimeInMillis();
        long yesterday = today - 24 * 60 * 60 * 1000;
        long week = today - (10 * 24 * 60 * 60 * 1000);
        if (time >= today) {
            return HEADER_TODAY;
        } else if (time >= yesterday) {
            return HEADER_YESTERDAY;
        } else if (time >= week) {
            return HEADER_THIS_WEEK;
        } else return HEADER_OLD;
    }

    class FeedbackHolder extends RecyclerView.ViewHolder {

        View headerRoot;
        View contentRoot;
        TextView headerText;

        FeedbackHolder(View itemView) {
            super(itemView);
            this.headerRoot = itemView.findViewById(R.id.header_root);
            this.contentRoot = itemView.findViewById(R.id.content_root);
            this.headerText = itemView.findViewById(R.id.item_feedback_header_title);
        }
    }

    class UsersHolder extends FeedbackHolder {

        ImageView uAvatar;
        TextView uName;
        TextView uInfo;
        TextView uTime;
        OnlineView uChangable;
        ImageView ivAttachment;

        UsersHolder(View root) {
            super(root);
            uAvatar = root.findViewById(R.id.item_friend_avatar);
            uName = root.findViewById(R.id.item_friend_name);
            uInfo = root.findViewById(R.id.item_additional_info);
            uTime = root.findViewById(R.id.item_friend_time);
            uChangable = root.findViewById(R.id.item_circle_friend_changable);
            ivAttachment = root.findViewById(R.id.item_feedback_user_attachment);
        }
    }

    class CommentHolder extends FeedbackHolder {

        ImageView cOwnerAvatar;
        OnlineView cChangable;
        TextView cOwnerName;
        TextView cOwnerText;
        TextView cOwnerTime;
        ImageView cReplyOwnerAvatar;
        ImageView cReplyChangable;
        TextView cReplyName;
        TextView cReplyText;
        TextView cReplyTime;
        ViewGroup cReplyContainer;
        View vAttachmentsRoot;
        View vReplyAttachmentsRoot;
        ImageView ivRightAttachment;

        CommentHolder(View root) {
            super(root);
            cOwnerAvatar = root.findViewById(R.id.item_comment_owner_avatar);
            cChangable = root.findViewById(R.id.item_circle_changable);
            cOwnerName = root.findViewById(R.id.item_comment_owner_name);
            cOwnerText = root.findViewById(R.id.item_comment_text);
            cOwnerTime = root.findViewById(R.id.item_comment_time);
            cReplyOwnerAvatar = root.findViewById(R.id.item_comment_reply_owner_avatar);
            cReplyChangable = root.findViewById(R.id.item_circle_reply);
            cReplyName = root.findViewById(R.id.item_comment_reply_owner_name);
            cReplyText = root.findViewById(R.id.item_comment_reply_text);
            cReplyTime = root.findViewById(R.id.item_comment_reply_time);
            cReplyContainer = root.findViewById(R.id.comment_reply_feedback);
            vAttachmentsRoot = root.findViewById(R.id.item_feedback_comment_attachments_root);
            vReplyAttachmentsRoot = root.findViewById(R.id.item_reply_comment_attachments_root);
            ivRightAttachment = root.findViewById(R.id.item_feedback_comment_attachment);
        }
    }
}
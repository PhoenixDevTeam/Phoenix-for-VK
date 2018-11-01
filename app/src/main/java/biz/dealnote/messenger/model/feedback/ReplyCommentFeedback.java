package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.ParcelableModelWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public final class ReplyCommentFeedback extends Feedback implements Parcelable {

    private AbsModel commentsOf;

    private Comment ownComment;

    private Comment feedbackComment;

    public ReplyCommentFeedback(@FeedbackType int type) {
        super(type);
    }

    private ReplyCommentFeedback(Parcel in) {
        super(in);
        commentsOf = ParcelableModelWrapper.readModel(in);
        ownComment = in.readParcelable(Comment.class.getClassLoader());
        feedbackComment = in.readParcelable(Comment.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, commentsOf);
        dest.writeParcelable(ownComment, flags);
        dest.writeParcelable(feedbackComment, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReplyCommentFeedback> CREATOR = new Creator<ReplyCommentFeedback>() {
        @Override
        public ReplyCommentFeedback createFromParcel(Parcel in) {
            return new ReplyCommentFeedback(in);
        }

        @Override
        public ReplyCommentFeedback[] newArray(int size) {
            return new ReplyCommentFeedback[size];
        }
    };

    public ReplyCommentFeedback setCommentsOf(AbsModel commentsOf) {
        this.commentsOf = commentsOf;
        return this;
    }

    public AbsModel getCommentsOf() {
        return commentsOf;
    }

    public Comment getOwnComment() {
        return ownComment;
    }

    public ReplyCommentFeedback setOwnComment(Comment ownComment) {
        this.ownComment = ownComment;
        return this;
    }

    public Comment getFeedbackComment() {
        return feedbackComment;
    }

    public ReplyCommentFeedback setFeedbackComment(Comment feedbackComment) {
        this.feedbackComment = feedbackComment;
        return this;
    }
}
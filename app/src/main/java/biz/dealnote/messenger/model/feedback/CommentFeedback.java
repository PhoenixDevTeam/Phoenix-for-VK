package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.ParcelableModelWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [comment_post, comment_photo, comment_video]
 */
public final class CommentFeedback extends Feedback implements Parcelable {

    private AbsModel commentOf;

    private Comment comment;

    public CommentFeedback(@FeedbackType int type) {
        super(type);
    }

    private CommentFeedback(Parcel in) {
        super(in);
        commentOf = ParcelableModelWrapper.readModel(in);
        comment = in.readParcelable(Comment.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, commentOf);
        dest.writeParcelable(comment, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CommentFeedback> CREATOR = new Creator<CommentFeedback>() {
        @Override
        public CommentFeedback createFromParcel(Parcel in) {
            return new CommentFeedback(in);
        }

        @Override
        public CommentFeedback[] newArray(int size) {
            return new CommentFeedback[size];
        }
    };

    public CommentFeedback setCommentOf(AbsModel commentOf) {
        this.commentOf = commentOf;
        return this;
    }

    public AbsModel getCommentOf() {
        return commentOf;
    }

    public Comment getComment() {
        return comment;
    }

    public CommentFeedback setComment(Comment comment) {
        this.comment = comment;
        return this;
    }
}
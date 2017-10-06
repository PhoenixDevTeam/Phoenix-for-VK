package biz.dealnote.messenger.model.feedback;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [comment_post, comment_photo, comment_video]
 */
public class CommentFeedback extends Feedback {

    private AbsModel commentOf;

    private Comment comment;

    public CommentFeedback(@FeedbackType int type) {
        super(type);
    }

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
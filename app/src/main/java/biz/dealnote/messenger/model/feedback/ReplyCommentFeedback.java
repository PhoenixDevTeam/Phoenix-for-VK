package biz.dealnote.messenger.model.feedback;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class ReplyCommentFeedback extends Feedback {

    private AbsModel commentsOf;

    private Comment ownComment;

    private Comment feedbackComment;

    public ReplyCommentFeedback(@FeedbackType int type) {
        super(type);
    }

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
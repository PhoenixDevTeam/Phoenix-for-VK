package biz.dealnote.messenger.model.feedback;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class MentionCommentFeedback extends Feedback {

    private Comment where;
    private AbsModel commentOf;

    // one of FeedbackType.MENTION_COMMENT_POST, FeedbackType.MENTION_COMMENT_PHOTO, FeedbackType.MENTION_COMMENT_VIDEO
    public MentionCommentFeedback(@FeedbackType int type) {
        super(type);
    }

    public MentionCommentFeedback setCommentOf(AbsModel commentOf) {
        this.commentOf = commentOf;
        return this;
    }

    public MentionCommentFeedback setWhere(Comment where) {
        this.where = where;
        return this;
    }

    public AbsModel getCommentOf() {
        return commentOf;
    }

    public Comment getWhere() {
        return where;
    }
}
package biz.dealnote.messenger.model.feedback;

import java.util.List;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Owner;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class LikeCommentFeedback extends Feedback {

    private Comment liked;
    private AbsModel commented;
    private List<Owner> owners;

    // one of FeedbackType.LIKE_COMMENT, FeedbackType.LIKE_COMMENT_PHOTO, FeedbackType.LIKE_COMMENT_TOPIC, FeedbackType.LIKE_COMMENT_VIDEO
    public LikeCommentFeedback(@FeedbackType int type) {
        super(type);
    }

    public AbsModel getCommented() {
        return commented;
    }

    public LikeCommentFeedback setCommented(AbsModel commented) {
        this.commented = commented;
        return this;
    }

    public LikeCommentFeedback setLiked(Comment liked) {
        this.liked = liked;
        return this;
    }

    public LikeCommentFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }

    public Comment getLiked() {
        return liked;
    }

    public List<Owner> getOwners() {
        return owners;
    }
}
package biz.dealnote.messenger.model.feedback;

import java.util.List;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Owner;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class LikeFeedback extends Feedback {

    private AbsModel liked;
    private List<Owner> owners;

    // one of FeedbackType.LIKE_PHOTO, FeedbackType.LIKE_POST, FeedbackType.LIKE_VIDEO
    public LikeFeedback(@FeedbackType int type) {
        super(type);
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public AbsModel getLiked() {
        return liked;
    }

    public LikeFeedback setLiked(AbsModel liked) {
        this.liked = liked;
        return this;
    }

    public LikeFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}
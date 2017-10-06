package biz.dealnote.messenger.model.feedback;

import biz.dealnote.messenger.model.AbsModel;

/**
 * Base class for types [mention, mention_comment_photo, mention_comment_video]
 * where - в каком обьекте было упоминание
 */
public class MentionFeedback extends Feedback {

    private AbsModel where;

    // one of FeedbackType.MENTION
    public MentionFeedback(@FeedbackType int type) {
        super(type);
    }

    public AbsModel getWhere() {
        return where;
    }

    public MentionFeedback setWhere(AbsModel where) {
        this.where = where;
        return this;
    }
}
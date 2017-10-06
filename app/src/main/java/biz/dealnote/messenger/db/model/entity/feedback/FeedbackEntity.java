package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.CommentEntity;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class FeedbackEntity {

    private final int type;

    private long date;

    private CommentEntity reply;

    public FeedbackEntity(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public FeedbackEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public CommentEntity getReply() {
        return reply;
    }

    public FeedbackEntity setReply(CommentEntity reply) {
        this.reply = reply;
        return this;
    }
}
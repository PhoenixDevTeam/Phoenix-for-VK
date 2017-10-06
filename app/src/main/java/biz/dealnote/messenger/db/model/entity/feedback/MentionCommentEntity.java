package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

/**
 * Base class for types [mention_comments, mention_comment_photo, mention_comment_video]
 */
public class MentionCommentEntity extends FeedbackEntity {

    private CommentEntity where;

    private EntityWrapper commented = EntityWrapper.empty();

    public MentionCommentEntity(int type) {
        super(type);
    }

    public MentionCommentEntity setCommented(Entity commented) {
        this.commented = new EntityWrapper(commented);
        return this;
    }

    public MentionCommentEntity setWhere(CommentEntity where) {
        this.where = where;
        return this;
    }

    public CommentEntity getWhere() {
        return where;
    }

    public Entity getCommented() {
        return commented.get();
    }
}
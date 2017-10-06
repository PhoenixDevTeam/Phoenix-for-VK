package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class LikeEntity extends FeedbackEntity {

    private int[] likesOwnerIds;

    private EntityWrapper liked = EntityWrapper.empty();

    public LikeEntity(int type) {
        super(type);
    }

    public LikeEntity setLiked(Entity liked) {
        this.liked = new EntityWrapper(liked);
        return this;
    }

    public LikeEntity setLikesOwnerIds(int[] likesOwnerIds) {
        this.likesOwnerIds = likesOwnerIds;
        return this;
    }

    public int[] getLikesOwnerIds() {
        return likesOwnerIds;
    }

    public Entity getLiked() {
        return liked.get();
    }
}
package biz.dealnote.messenger.db.model.entity.feedback;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for [follow, friend_accepted]
 */
public class UsersEntity extends FeedbackEntity {

    private int[] ids;

    public UsersEntity(int type) {
        super(type);
    }

    public UsersEntity setOwners(int[] ids) {
        this.ids = ids;
        return this;
    }

    public int[] getOwners() {
        return ids;
    }
}
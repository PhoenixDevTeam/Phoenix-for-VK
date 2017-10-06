package biz.dealnote.messenger.db.model;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public class IdPairEntity {

    private final int id;

    private final int ownerId;

    public IdPairEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }
}
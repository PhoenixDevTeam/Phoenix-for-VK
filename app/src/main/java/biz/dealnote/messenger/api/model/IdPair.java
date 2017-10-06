package biz.dealnote.messenger.api.model;

/**
 * Created by admin on 02.01.2017.
 * phoenix
 */
public class IdPair {

    public final int id;

    public final int ownerId;

    public IdPair(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }
}

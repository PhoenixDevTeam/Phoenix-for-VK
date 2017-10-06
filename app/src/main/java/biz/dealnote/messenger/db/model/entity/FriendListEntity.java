package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public class FriendListEntity {

    private final int id;

    private final String name;

    public FriendListEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
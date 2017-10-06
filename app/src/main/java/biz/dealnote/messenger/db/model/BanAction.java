package biz.dealnote.messenger.db.model;

/**
 * Created by Ruslan Kolbasa on 19.06.2017.
 * phoenix
 */
public class BanAction {

    private final int groupId;

    private final int userId;

    private final boolean ban;

    public BanAction(int groupId, int userId, boolean ban) {
        this.groupId = groupId;
        this.userId = userId;
        this.ban = ban;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isBan() {
        return ban;
    }
}
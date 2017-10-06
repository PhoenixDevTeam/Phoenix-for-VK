package biz.dealnote.messenger.model;

import java.util.List;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public class BannedPart {

    private final int totalCount;

    private final List<User> users;

    public BannedPart(int totalCount, List<User> users) {
        this.totalCount = totalCount;
        this.users = users;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public List<User> getUsers() {
        return users;
    }
}
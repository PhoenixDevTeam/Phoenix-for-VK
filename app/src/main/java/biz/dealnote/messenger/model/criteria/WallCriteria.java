package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;

/**
 * Created by ruslan.kolbasa on 06-Jun-16.
 * phoenix
 */
public class WallCriteria extends Criteria {

    public static final int MODE_ALL = 0;
    public static final int MODE_OWNER = 1;
    public static final int MODE_SCHEDULED = 2;
    public static final int MODE_SUGGEST = 3;

    private final int accountId;

    private DatabaseIdRange range;

    private int ownerId;

    private int mode;

    public WallCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.mode = MODE_ALL;
    }

    public int getAccountId() {
        return accountId;
    }

    public WallCriteria setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public int getMode() {
        return mode;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public WallCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public String toString() {
        return "WallCriteria{" +
                "accountId=" + accountId +
                ", range=" + range +
                ", ownerId=" + ownerId +
                ", mode=" + mode +
                '}';
    }
}

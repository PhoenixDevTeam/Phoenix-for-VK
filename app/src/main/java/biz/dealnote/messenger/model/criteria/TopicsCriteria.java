package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;

/**
 * Created by admin on 13.12.2016.
 * phoenix
 */
public class TopicsCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private DatabaseIdRange range;

    public TopicsCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public TopicsCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getOwnerId() {
        return ownerId;
    }
}

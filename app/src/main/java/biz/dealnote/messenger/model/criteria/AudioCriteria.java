package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.model.Criteria;

/**
 * Created by admin on 22.11.2016.
 * phoenix
 */
public class AudioCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private DatabaseIdRange range;

    public AudioCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public int getAccountId() {
        return accountId;
    }

    public AudioCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }
}

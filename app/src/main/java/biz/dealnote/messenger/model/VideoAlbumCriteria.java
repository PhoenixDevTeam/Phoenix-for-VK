package biz.dealnote.messenger.model;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.model.criteria.Criteria;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideoAlbumCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private DatabaseIdRange range;

    public VideoAlbumCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public VideoAlbumCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }
}

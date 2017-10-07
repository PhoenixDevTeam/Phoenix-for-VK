package biz.dealnote.messenger.model;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.model.criteria.Criteria;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideoCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private final int albumId;

    private DatabaseIdRange range;

    public VideoCriteria(int accountId, int ownerId, int albumId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        this.albumId = albumId;
    }

    public VideoCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }
}

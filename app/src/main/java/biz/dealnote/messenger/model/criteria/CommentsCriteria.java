package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.model.Commented;

/**
 * Created by ruslan.kolbasa on 28.11.2016.
 * phoenix
 */
public class CommentsCriteria {

    private final Commented commented;

    private final int accountId;

    public CommentsCriteria(int accountId, Commented commented) {
        this.accountId = accountId;
        this.commented = commented;
    }

    public Commented getCommented() {
        return commented;
    }

    public int getAccountId() {
        return accountId;
    }

    public DatabaseIdRange range;

    public CommentsCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public DatabaseIdRange getRange() {
        return range;
    }
}
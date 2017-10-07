package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;

/**
 * Created by ruslan.kolbasa on 13-Jun-16.
 * phoenix
 */
public class NotificationsCriteria extends Criteria {

    private final int accountId;

    private DatabaseIdRange range;

    public NotificationsCriteria(int accountId) {
        this.accountId = accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public int getAccountId() {
        return accountId;
    }

    public NotificationsCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationsCriteria that = (NotificationsCriteria) o;

        return accountId == that.accountId
                && (range != null ? range.equals(that.range) : that.range == null);
    }

    @Override
    public int hashCode() {
        int result = accountId;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        return result;
    }
}

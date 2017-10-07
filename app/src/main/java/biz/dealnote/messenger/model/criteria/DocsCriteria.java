package biz.dealnote.messenger.model.criteria;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class DocsCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private Integer filter;

    public DocsCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public DocsCriteria setFilter(Integer filter) {
        this.filter = filter;
        return this;
    }

    public Integer getFilter() {
        return filter;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAccountId() {
        return accountId;
    }
}

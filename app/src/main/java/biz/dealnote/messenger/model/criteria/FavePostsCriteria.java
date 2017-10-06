package biz.dealnote.messenger.model.criteria;

/**
 * Created by hp-dv6 on 28.05.2016.
 * VKMessenger
 */
public class FavePostsCriteria {

    private final int accountId;

    public FavePostsCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
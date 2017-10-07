package biz.dealnote.messenger.model.criteria;

/**
 * Created by hp-dv6 on 04.06.2016.
 * VKMessenger
 */
public class DialogsCriteria extends Criteria {

    private final int accountId;

    public DialogsCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}

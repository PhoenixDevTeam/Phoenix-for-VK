package biz.dealnote.messenger.model;

/**
 * Created by ruslan.kolbasa on 22.11.2016.
 * phoenix
 */
public class WriteText {

    private final int accountId;

    private final int userId;

    private final int peerId;

    public WriteText(int accountId, int userId, int peerId) {
        this.accountId = accountId;
        this.userId = userId;
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAccountId() {
        return accountId;
    }
}
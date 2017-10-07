package biz.dealnote.messenger.model.criteria;

public class PhotoAlbumsCriteria extends Criteria {

    private int accountId;
    private int ownerId;

    public PhotoAlbumsCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAccountId() {
        return accountId;
    }
}

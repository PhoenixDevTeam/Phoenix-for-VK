package biz.dealnote.messenger.model.criteria;

import biz.dealnote.messenger.db.DatabaseIdRange;

public class PhotoCriteria{

    private final int accountId;
    private Integer ownerId;
    private Integer albumId;
    private String orderBy;

    private DatabaseIdRange range;

    public PhotoCriteria(int accountId) {
        this.accountId = accountId;
    }

    public PhotoCriteria setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public PhotoCriteria setAlbumId(Integer albumId) {
        this.albumId = albumId;
        return this;
    }

    public PhotoCriteria setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public PhotoCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public String getOrderBy() {
        return orderBy;
    }
}
package biz.dealnote.messenger.db.model.entity;

public class FaveUserEntity extends UserEntity {

    private String description;

    private String type;

    private long updateDate;

    public FaveUserEntity(int id) {
        super(id);
    }

    public String getDescription() {
        return description;
    }

    public FaveUserEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFaveType() {
        return type;
    }

    public FaveUserEntity setFaveType(String type) {
        this.type = type;
        return this;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public FaveUserEntity setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
        return this;
    }
}

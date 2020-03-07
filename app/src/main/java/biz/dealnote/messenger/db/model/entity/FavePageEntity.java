package biz.dealnote.messenger.db.model.entity;

import biz.dealnote.messenger.model.FavePageType;

public class FavePageEntity {
    private int id;

    private String description;

    @FavePageType
    private String type;

    private long updateDate;

    public FavePageEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public FavePageEntity setDescription(String description) {
        this.description = description;
        return this;
    }


    public String getFaveType() {
        return type;
    }

    public FavePageEntity setFaveType(String type) {
        this.type = type;
        return this;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public FavePageEntity setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
        return this;
    }
}

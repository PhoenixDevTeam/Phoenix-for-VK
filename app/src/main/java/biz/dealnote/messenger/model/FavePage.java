package biz.dealnote.messenger.model;

import biz.dealnote.messenger.api.model.Identificable;

public class FavePage implements Identificable {
    private int id;

    private String description;

    private String type;

    private long updateDate;

    private User user;

    private Community group;

    public FavePage(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public FavePage setId(int id) {
        this.id = id;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FavePage setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getType() {
        return type;
    }

    public FavePage setFaveType(String type) {
        this.type = type;
        return this;
    }

    public long getUpdatedDate() {
        return updateDate;
    }

    public FavePage setUpdatedDate(long updateDate) {
        this.updateDate = updateDate;
        return this;
    }

    public User getUser() {
        return user;
    }

    public FavePage setUser(User user) {
        this.user = user;
        return this;
    }

    public Community getGroup() {
        return group;
    }

    public FavePage setGroup(Community group) {
        this.group = group;
        return this;
    }
}

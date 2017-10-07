package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class FaveLinkEntity {

    private final String id;

    private final String url;

    private String title;

    private String description;

    private String photo50;

    private String photo100;

    public FaveLinkEntity(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public FaveLinkEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FaveLinkEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public FaveLinkEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public FaveLinkEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }
}
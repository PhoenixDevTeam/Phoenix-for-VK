package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class CityEntity {

    private int id;

    private String title;

    private boolean important;

    private String area;

    private String region;

    public int getId() {
        return id;
    }

    public boolean isImportant() {
        return important;
    }

    public CityEntity setArea(String area) {
        this.area = area;
        return this;
    }

    public CityEntity setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public CityEntity setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getArea() {
        return area;
    }

    public String getRegion() {
        return region;
    }

    public CityEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CityEntity setTitle(String title) {
        this.title = title;
        return this;
    }
}
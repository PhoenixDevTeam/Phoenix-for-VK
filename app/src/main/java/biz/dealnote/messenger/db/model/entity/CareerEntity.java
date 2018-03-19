package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class CareerEntity {

    private int groupId;

    private String company;

    private int countryId;

    private int cityId;

    private int from;

    private int until;

    private String position;

    public CareerEntity setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public CareerEntity setCompany(String company) {
        this.company = company;
        return this;
    }

    public CareerEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public CareerEntity setFrom(int from) {
        this.from = from;
        return this;
    }

    public CareerEntity setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    public CareerEntity setPosition(String position) {
        this.position = position;
        return this;
    }

    public CareerEntity setUntil(int until) {
        this.until = until;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public int getCountryId() {
        return countryId;
    }

    public int getFrom() {
        return from;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getUntil() {
        return until;
    }

    public String getCompany() {
        return company;
    }

    public String getPosition() {
        return position;
    }
}
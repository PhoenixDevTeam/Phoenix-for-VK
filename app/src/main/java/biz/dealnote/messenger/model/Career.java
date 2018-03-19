package biz.dealnote.messenger.model;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class Career {

    private Community group;

    private String company;

    private int countryId;

    private int cityId;

    private int from;

    private int until;

    private String position;

    public Career setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public Career setCompany(String company) {
        this.company = company;
        return this;
    }

    public Career setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public Career setFrom(int from) {
        this.from = from;
        return this;
    }

    public Career setGroup(Community group) {
        this.group = group;
        return this;
    }

    public Career setPosition(String position) {
        this.position = position;
        return this;
    }

    public Career setUntil(int until) {
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

    public Community getGroup() {
        return group;
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
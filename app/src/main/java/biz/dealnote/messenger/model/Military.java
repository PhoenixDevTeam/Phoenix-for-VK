package biz.dealnote.messenger.model;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class Military {

    private String unit;

    private int unitId;

    private int countryId;

    private int from;

    private int until;

    public Military setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public Military setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public Military setFrom(int from) {
        this.from = from;
        return this;
    }

    public Military setUnitId(int unitId) {
        this.unitId = unitId;
        return this;
    }

    public Military setUntil(int until) {
        this.until = until;
        return this;
    }

    public int getUntil() {
        return until;
    }

    public int getFrom() {
        return from;
    }

    public int getCountryId() {
        return countryId;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getUnit() {
        return unit;
    }
}
package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class MilitaryEntity {

    private String unit;

    private int unitId;

    private int countryId;

    private int from;

    private int until;

    public MilitaryEntity setUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public MilitaryEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public MilitaryEntity setFrom(int from) {
        this.from = from;
        return this;
    }

    public MilitaryEntity setUnitId(int unitId) {
        this.unitId = unitId;
        return this;
    }

    public MilitaryEntity setUntil(int until) {
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
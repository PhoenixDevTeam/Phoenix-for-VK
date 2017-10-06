package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.model.VKApiCareer;

public class UserCareerColumns implements BaseColumns {

    private UserCareerColumns() {}

    public static final String TABLENAME = "user_career";

    public static final String USER_ID = "user_id";
    public static final String GROUP_ID = "group_id";
    public static final String COMPANY = "company";
    public static final String COUNTRY_ID = "country_id";
    public static final String CITY_ID = "city_id";
    public static final String CITY_NAME = "city_name";
    public static final String YEAR_FROM = "year_from";
    public static final String YEAR_UNTIL = "year_until";
    public static final String POSITION = "position";

    public static ContentValues getCV(@NonNull VKApiCareer career, int userId){
        ContentValues cv = new ContentValues();
        cv.put(USER_ID, userId);
        cv.put(GROUP_ID, career.group_id);
        cv.put(COMPANY, career.company);
        cv.put(COUNTRY_ID, career.country_id);
        cv.put(CITY_ID, career.city_id);
        cv.put(CITY_NAME, career.city_name);
        cv.put(YEAR_FROM, career.from);
        cv.put(YEAR_UNTIL, career.until);
        cv.put(POSITION, career.position);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_USER_ID = TABLENAME + "." + USER_ID;
    public static final String FULL_GROUP_ID = TABLENAME + "." + GROUP_ID;
    public static final String FULL_COMPANY = TABLENAME + "." + COMPANY;
    public static final String FULL_COUNTRY_ID = TABLENAME + "." + COUNTRY_ID;
    public static final String FULL_CITY_ID = TABLENAME + "." + CITY_ID;
    public static final String FULL_CITY_NAME = TABLENAME + "." + CITY_NAME;
    public static final String FULL_YEAR_FROM = TABLENAME + "." + YEAR_FROM;
    public static final String FULL_YEAR_UNTIL = TABLENAME + "." + YEAR_UNTIL;
    public static final String FULL_POSITION = TABLENAME + "." + POSITION;
}

package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.google.gson.Gson;

import biz.dealnote.messenger.api.model.VKApiCommunity;

public class GroupsDetColumns implements BaseColumns {

    private static final Gson GSON = new Gson();

    private GroupsDetColumns() {}

    public static final String TABLENAME = "groups_det";

    public static final String BLACKLISTED = "blacklisted";
    public static final String BAN_END_DATE = "ban_end_date";
    public static final String BAN_COMEMNT = "comment";
    public static final String CITY_ID = "city_id";
    public static final String COUNTRY_ID = "country_id";
    public static final String GEO_ID = "geo_id";
    public static final String DESCRIPTION = "description";
    public static final String WIKI_PAGE = "wiki_page";
    public static final String MEMBERS_COUNT = "members_count";
    public static final String COUNTERS = "counters";
    public static final String START_DATE = "start_date";
    public static final String FINISH_DATE = "finish_date";
    public static final String CAN_POST = "can_post";
    public static final String CAN_SEE_ALL_POSTS = "can_see_all_posts";
    public static final String CAN_UPLOAD_DOC = "can_upload_doc";
    public static final String CAN_UPLOAD_VIDEO = "can_upload_video";
    public static final String CAN_CREATE_TOPIC = "can_create_topic";
    public static final String ACTIVITY = "activity";
    public static final String STATUS = "status";
    public static final String FIXED_POST = "fixed_post";
    public static final String VERIFIED = "verified";
    public static final String SITE = "site";
    public static final String MAIN_ALBUM_ID = "main_album_id";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String LINKS_COUNT = "links_count";
    public static final String CONTACTS_COUNT = "contacts_count";
    public static final String CAN_MESSAGE = "can_message";

    public static ContentValues getCV(VKApiCommunity c){
        ContentValues cv = new ContentValues();
        cv.put(_ID, c.id);
        cv.put(BLACKLISTED, c.blacklisted);
        cv.put(BAN_END_DATE, c.ban_end_date);
        cv.put(BAN_COMEMNT, c.ban_comment);

        if(c.city != null){
            cv.put(CITY_ID, c.city.id);
        }

        if(c.country != null){
            cv.put(COUNTRY_ID, c.country.id);
        }

        if(c.place != null){
            cv.put(GEO_ID, c.place.id);
        }

        cv.put(DESCRIPTION, c.description);
        cv.put(WIKI_PAGE, c.wiki_page);
        cv.put(MEMBERS_COUNT, c.members_count);
        cv.put(COUNTERS, c.counters == null ? null : GSON.toJson(c.counters));
        cv.put(START_DATE, c.start_date);
        cv.put(FINISH_DATE, c.finish_date);
        cv.put(CAN_POST, c.can_post);
        cv.put(CAN_SEE_ALL_POSTS, c.can_see_all_posts);
        cv.put(CAN_UPLOAD_DOC, c.can_upload_doc);
        cv.put(CAN_UPLOAD_VIDEO, c.can_upload_video);
        cv.put(CAN_CREATE_TOPIC, c.can_create_topic);
        cv.put(ACTIVITY, c.activity);
        cv.put(STATUS, c.status);
        cv.put(FIXED_POST, c.fixed_post);
        cv.put(VERIFIED, c.verified);
        cv.put(SITE, c.site);
        cv.put(MAIN_ALBUM_ID, c.main_album_id);
        cv.put(IS_FAVORITE, c.is_favorite);
        cv.put(LINKS_COUNT, c.links != null ? c.links.size() : 0);
        cv.put(CONTACTS_COUNT, c.contacts != null ? c.contacts.size() : 0);
        cv.put(CAN_MESSAGE, c.can_message);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_BLACKLISTED = TABLENAME + "." + BLACKLISTED;
    public static final String FULL_BAN_END_DATE = TABLENAME + "." + BAN_END_DATE;
    public static final String FULL_BAN_COMEMNT = TABLENAME + "." + BAN_COMEMNT;
    public static final String FULL_CITY_ID = TABLENAME + "." + CITY_ID;
    public static final String FULL_COUNTRY_ID = TABLENAME + "." + COUNTRY_ID;
    public static final String FULL_GEO_ID = TABLENAME + "." + GEO_ID;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_WIKI_PAGE = TABLENAME + "." + WIKI_PAGE;
    public static final String FULL_MEMBERS_COUNT = TABLENAME + "." + MEMBERS_COUNT;
    public static final String FULL_COUNTERS = TABLENAME + "." + COUNTERS;
    public static final String FULL_START_DATE = TABLENAME + "." + START_DATE;
    public static final String FULL_FINISH_DATE = TABLENAME + "." + FINISH_DATE;
    public static final String FULL_CAN_POST = TABLENAME + "." + CAN_POST;
    public static final String FULL_CAN_SEE_ALL_POSTS = TABLENAME + "." + CAN_SEE_ALL_POSTS;
    public static final String FULL_CAN_UPLOAD_DOC = TABLENAME + "." + CAN_UPLOAD_DOC;
    public static final String FULL_CAN_UPLOAD_VIDEO = TABLENAME + "." + CAN_UPLOAD_VIDEO;
    public static final String FULL_CAN_CREATE_TOPIC = TABLENAME + "." + CAN_CREATE_TOPIC;
    public static final String FULL_ACTIVITY = TABLENAME + "." + ACTIVITY;
    public static final String FULL_STATUS = TABLENAME + "." + STATUS;
    public static final String FULL_FIXED_POST = TABLENAME + "." + FIXED_POST;
    public static final String FULL_VERIFIED = TABLENAME + "." + VERIFIED;
    public static final String FULL_SITE = TABLENAME + "." + SITE;
    public static final String FULL_MAIN_ALBUM_ID = TABLENAME + "." + MAIN_ALBUM_ID;
    public static final String FULL_IS_FAVORITE = TABLENAME + "." + IS_FAVORITE;
    public static final String FULL_LINKS_COUNT = TABLENAME + "." + LINKS_COUNT;
    public static final String FULL_CONTACTS_COUNT = TABLENAME + "." + CONTACTS_COUNT;
    public static final String FULL_CAN_MESSAGE = TABLENAME + "." + CAN_MESSAGE;
}

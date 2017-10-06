package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.model.FaveLinkDto;

public final class FaveLinksColumns implements BaseColumns {

    private FaveLinksColumns(){}

    public static final String TABLENAME = "fave_link";

    public static final String LINK_ID = "link_id";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";

    public static ContentValues buildCV(@NonNull FaveLinkDto link){
        ContentValues cv = new ContentValues();
        cv.put(LINK_ID, link.id);
        cv.put(URL, link.url);
        cv.put(TITLE, link.title);
        cv.put(DESCRIPTION, link.description);
        cv.put(PHOTO_50, link.photo_50);
        cv.put(PHOTO_100, link.photo_100);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_LINK_ID = TABLENAME + "." + LINK_ID;
    public static final String FULL_URL = TABLENAME + "." + URL;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;

}
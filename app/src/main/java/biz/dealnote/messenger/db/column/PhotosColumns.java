package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.google.gson.Gson;

import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public final class PhotosColumns implements BaseColumns {

    private PhotosColumns(){}

    public static final String TABLENAME = "photos";
    //Columns
    public static final String PHOTO_ID = "photo_id";
    public static final String ALBUM_ID = "album_id";
    public static final String OWNER_ID = "owner_id";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String TEXT = "text";
    public static final String DATE = "date";
    public static final String SIZES = "sizes";
    public static final String USER_LIKES = "user_likes";
    public static final String CAN_COMMENT = "can_comment";
    public static final String LIKES = "likes";
    public static final String COMMENTS = "comments";
    public static final String TAGS = "tags";
    public static final String ACCESS_KEY = "access_key";

    private static final Gson GSON = new Gson();

    public static ContentValues getCV(VKApiPhoto p){
        ContentValues cv = new ContentValues();
        cv.put(PHOTO_ID, p.id);
        cv.put(ALBUM_ID, p.album_id);
        cv.put(OWNER_ID, p.owner_id);
        cv.put(WIDTH, p.width);
        cv.put(HEIGHT, p.height);
        cv.put(TEXT, p.text);
        cv.put(DATE, p.date);

        if(nonNull(p.sizes)){
            cv.put(SIZES, GSON.toJson(Dto2Model.transform(p.sizes)));
        }

        cv.put(USER_LIKES, p.user_likes);
        cv.put(CAN_COMMENT, p.can_comment);
        cv.put(LIKES, p.likes);
        cv.put(COMMENTS, isNull(p.comments) ? 0 : p.comments.count);
        cv.put(TAGS, p.tags);

        if(!safeIsEmpty(p.access_key)){
            cv.put(ACCESS_KEY, p.access_key);
        }

        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_PHOTO_ID = TABLENAME + "." + PHOTO_ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_WIDTH = TABLENAME + "." + WIDTH;
    public static final String FULL_HEIGHT = TABLENAME + "." + HEIGHT;
    public static final String FULL_TEXT = TABLENAME + "." + TEXT;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_SIZES = TABLENAME + "." + SIZES;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_CAN_COMMENT = TABLENAME + "." + CAN_COMMENT;
    public static final String FULL_LIKES = TABLENAME + "." + LIKES;
    public static final String FULL_COMMENTS = TABLENAME + "." + COMMENTS;
    public static final String FULL_TAGS = TABLENAME + "." + TAGS;
    public static final String FULL_ACCESS_KEY = TABLENAME + "." + ACCESS_KEY;
}

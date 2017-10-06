package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

import biz.dealnote.messenger.api.model.VKApiVideo;

import static biz.dealnote.messenger.util.Objects.isNull;

public final class VideoColumns implements BaseColumns {

    public static final String TABLENAME = "videos";

    public static final String VIDEO_ID = "video_id";
    public static final String OWNER_ID = "owner_id";
    public static final String ORIGINAL_OWNER_ID = "original_owner_id";
    public static final String ALBUM_ID = "album_id";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String DURATION = "duration";
    public static final String LINK = "link";
    public static final String DATE = "date";
    public static final String ADDING_DATE = "adding_date";
    public static final String VIEWS = "views";
    public static final String PLAYER = "player";
    public static final String PHOTO_130 = "photo_130";
    public static final String PHOTO_320 = "photo_320";
    public static final String PHOTO_800 = "photo_800";
    public static final String ACCESS_KEY = "access_key";
    public static final String COMMENTS = "comments";
    public static final String CAN_COMENT = "can_comment";
    public static final String CAN_REPOST = "can_repost";
    public static final String USER_LIKES = "user_likes";
    public static final String REPEAT = "repeat";
    public static final String LIKES = "likes";
    public static final String PRIVACY_VIEW = "privacy_view";
    public static final String PRIVACY_COMMENT = "privacy_comment";
    public static final String MP4_240 = "mp4_240";
    public static final String MP4_360 = "mp4_360";
    public static final String MP4_480 = "mp4_480";
    public static final String MP4_720 = "mp4_720";
    public static final String MP4_1080 = "mp4_1080";
    public static final String EXTERNAL = "external";
    public static final String PLATFORM = "platform";
    public static final String CAN_EDIT = "can_edit";
    public static final String CAN_ADD = "can_add";

    /* Дело в том, что вк передает в p.owner_id идентификатор оригинального владельца.
     * Поэтому необходимо отдельно сохранять идентикатор owner-а, у кого в видеозаписях мы нашли видео */
    public static ContentValues getCV(VKApiVideo p, int ownerId){
        ContentValues cv = new ContentValues();
        cv.put(VIDEO_ID, p.id);
        cv.put(OWNER_ID, ownerId);
        cv.put(ORIGINAL_OWNER_ID, p.owner_id);
        cv.put(ALBUM_ID, p.album_id);
        cv.put(TITLE, p.title);
        cv.put(DESCRIPTION, p.description);
        cv.put(DURATION, p.duration);
        cv.put(LINK, p.link);
        cv.put(DATE, p.date);
        cv.put(ADDING_DATE, p.adding_date);
        cv.put(VIEWS, p.views);
        cv.put(PLAYER, p.player);
        cv.put(PHOTO_130, p.photo_130);
        cv.put(PHOTO_320, p.photo_320);
        cv.put(PHOTO_800, p.photo_800);
        cv.put(ACCESS_KEY, p.access_key);
        cv.put(COMMENTS, isNull(p.comments) ? 0 : p.comments.count);
        cv.put(CAN_COMENT, p.can_comment);
        cv.put(CAN_REPOST, p.can_repost);
        cv.put(USER_LIKES, p.user_likes);
        cv.put(REPEAT, p.repeat);
        cv.put(LIKES, p.likes);
        cv.put(PRIVACY_VIEW, p.privacy_view == null ? null : p.privacy_view.toString());
        cv.put(PRIVACY_COMMENT, p.privacy_comment == null ? null : p.privacy_comment.toString());
        cv.put(MP4_240, p.mp4_240);
        cv.put(MP4_360, p.mp4_360);
        cv.put(MP4_480, p.mp4_480);
        cv.put(MP4_720, p.mp4_720);
        cv.put(MP4_1080, p.mp4_1080);
        cv.put(EXTERNAL, p.external);
        cv.put(PLATFORM, p.platform);
        cv.put(CAN_EDIT, p.can_edit);
        cv.put(CAN_ADD, p.can_add);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_VIDEO_ID = TABLENAME + "." + VIDEO_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_ORIGINAL_OWNER_ID = TABLENAME + "." + ORIGINAL_OWNER_ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_DURATION = TABLENAME + "." + DURATION;
    public static final String FULL_LINK = TABLENAME + "." + LINK;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_ADDING_DATE = TABLENAME + "." + ADDING_DATE;
    public static final String FULL_VIEWS = TABLENAME + "." + VIEWS;
    public static final String FULL_PLAYER = TABLENAME + "." + PLAYER;
    public static final String FULL_PHOTO_130 = TABLENAME + "." + PHOTO_130;
    public static final String FULL_PHOTO_320 = TABLENAME + "." + PHOTO_320;
    public static final String FULL_PHOTO_800 = TABLENAME + "." + PHOTO_800;
    public static final String FULL_ACCESS_KEY = TABLENAME + "." + ACCESS_KEY;
    public static final String FULL_COMMENTS = TABLENAME + "." + COMMENTS;
    public static final String FULL_CAN_COMENT = TABLENAME + "." + CAN_COMENT;
    public static final String FULL_CAN_REPOST = TABLENAME + "." + CAN_REPOST;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_REPEAT = TABLENAME + "." + REPEAT;
    public static final String FULL_LIKES = TABLENAME + "." + LIKES;
    public static final String FULL_PRIVACY_VIEW = TABLENAME + "." + PRIVACY_VIEW;
    public static final String FULL_PRIVACY_COMMENT = TABLENAME + "." + PRIVACY_COMMENT;
    public static final String FULL_MP4_240 = TABLENAME + "." + MP4_240;
    public static final String FULL_MP4_360 = TABLENAME + "." + MP4_360;
    public static final String FULL_MP4_480 = TABLENAME + "." + MP4_480;
    public static final String FULL_MP4_720 = TABLENAME + "." + MP4_720;
    public static final String FULL_MP4_1080 = TABLENAME + "." + MP4_1080;
    public static final String FULL_EXTERNAL = TABLENAME + "." + EXTERNAL;
    public static final String FULL_PLATFORM = TABLENAME + "." + PLATFORM;
    public static final String FULL_CAN_EDIT = TABLENAME + "." + CAN_EDIT;
    public static final String FULL_CAN_ADD = TABLENAME + "." + CAN_ADD;

}

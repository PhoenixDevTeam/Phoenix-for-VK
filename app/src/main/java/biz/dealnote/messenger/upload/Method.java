package biz.dealnote.messenger.upload;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Method.PHOTO_TO_ALBUM,
        Method.PHOTO_TO_WALL,
        Method.PHOTO_TO_COMMENT,
        Method.PHOTO_TO_PROFILE,
        Method.PHOTO_TO_MESSAGE,
        Method.AUDIO,
        Method.VIDEO,
        Method.DOCUMENT})
@Retention(RetentionPolicy.SOURCE)
public @interface Method {
    int PHOTO_TO_ALBUM = 1;
    int PHOTO_TO_WALL = 2;
    int PHOTO_TO_COMMENT = 3;
    int PHOTO_TO_PROFILE = 4;
    int PHOTO_TO_MESSAGE = 6;
    int AUDIO = 7;
    int VIDEO = 8;
    int DOCUMENT = 9;
}


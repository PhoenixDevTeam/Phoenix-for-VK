package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({SwitchableCategory.FRIENDS,
        SwitchableCategory.DIALOGS,
        SwitchableCategory.FEED,
        SwitchableCategory.FEEDBACK,
        SwitchableCategory.GROUPS,
        SwitchableCategory.PHOTOS,
        SwitchableCategory.VIDEOS,
        SwitchableCategory.MUSIC,
        SwitchableCategory.DOCS,
        SwitchableCategory.BOOKMARKS,
        SwitchableCategory.SEARCH,
        SwitchableCategory.NEWSFEED_COMMENTS})
@Retention(RetentionPolicy.SOURCE)
public @interface SwitchableCategory {
    int FRIENDS = 1;
    int DIALOGS = 2;
    int FEED = 3;
    int FEEDBACK = 4;
    int NEWSFEED_COMMENTS = 12;
    int GROUPS = 5;
    int PHOTOS = 6;
    int VIDEOS = 7;
    int MUSIC = 8;
    int DOCS = 9;
    int BOOKMARKS = 10;
    int SEARCH = 11;
}

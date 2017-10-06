package biz.dealnote.messenger.fragment.search;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({SearchContentType.PEOPLE, SearchContentType.COMMUNITIES, SearchContentType.NEWS,
        SearchContentType.VIDEOS, SearchContentType.MESSAGES, SearchContentType.DOCUMENTS,
        SearchContentType.WALL, SearchContentType.DIALOGS})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchContentType {
    int PEOPLE = 0;
    int COMMUNITIES = 1;
    int NEWS = 2;
    int VIDEOS = 3;
    int MESSAGES = 4;
    int DOCUMENTS = 5;
    int WALL = 6;
    int DIALOGS = 7;
}

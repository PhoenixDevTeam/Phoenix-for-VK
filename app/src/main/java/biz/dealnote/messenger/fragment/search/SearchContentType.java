package biz.dealnote.messenger.fragment.search;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({SearchContentType.PEOPLE, SearchContentType.COMMUNITIES, SearchContentType.NEWS,
        SearchContentType.AUDIOS, SearchContentType.VIDEOS, SearchContentType.MESSAGES, SearchContentType.DOCUMENTS,
        SearchContentType.WALL, SearchContentType.DIALOGS})
@Retention(RetentionPolicy.SOURCE)
public @interface SearchContentType {
    int PEOPLE = 0;
    int COMMUNITIES = 1;
    int NEWS = 2;
    int AUDIOS = 3;
    int VIDEOS = 4;
    int MESSAGES = 5;
    int DOCUMENTS = 6;
    int WALL = 7;
    int DIALOGS = 8;
}

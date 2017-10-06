package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.model.VkApiFeedList;

public class FeedListsColumns implements BaseColumns {

    private FeedListsColumns(){}

    public static final String TABLENAME = "feed_sources";

    public static final String TITLE = "title";
    public static final String NO_REPOSTS = "no_reposts";
    public static final String SOURCE_IDS = "source_ids";

    public static ContentValues getCV(@NonNull VkApiFeedList vkApiFeedList){
        ContentValues cv = new ContentValues();
        cv.put(_ID, vkApiFeedList.id);
        cv.put(TITLE, vkApiFeedList.title);
        cv.put(NO_REPOSTS, vkApiFeedList.no_reposts);

        String sources = null;
        if(vkApiFeedList.source_ids != null){
            sources = "";
            for(int i = 0; i < vkApiFeedList.source_ids.length; i++){
                sources = sources + vkApiFeedList.source_ids[i];

                if(i != vkApiFeedList.source_ids.length - 1){
                    sources = sources + ",";
                }
            }
        }

        cv.put(SOURCE_IDS, sources);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_NO_REPOSTS = TABLENAME + "." + NO_REPOSTS;
    public static final String FULL_SOURCE_IDS = TABLENAME + "." + SOURCE_IDS;

}

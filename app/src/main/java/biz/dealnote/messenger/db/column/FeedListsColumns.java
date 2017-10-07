package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.db.model.entity.FeedListEntity;
import biz.dealnote.messenger.util.Objects;

public class FeedListsColumns implements BaseColumns {

    private FeedListsColumns(){}

    public static final String TABLENAME = "feed_sources";

    public static final String TITLE = "title";
    public static final String NO_REPOSTS = "no_reposts";
    public static final String SOURCE_IDS = "source_ids";

    public static ContentValues getCV(@NonNull FeedListEntity entity){
        ContentValues cv = new ContentValues();
        cv.put(_ID, entity.getId());
        cv.put(TITLE, entity.getTitle());
        cv.put(NO_REPOSTS, entity.isNoReposts());

        String sources = null;
        int[] ids = entity.getSourceIds();

        if(Objects.nonNull(ids)){
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < ids.length; i++){
                builder.append(ids[i]);

                if(i != ids.length - 1){
                    builder.append(",");
                }
            }

            sources = builder.toString();
        }

        cv.put(SOURCE_IDS, sources);
        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_NO_REPOSTS = TABLENAME + "." + NO_REPOSTS;
    public static final String FULL_SOURCE_IDS = TABLENAME + "." + SOURCE_IDS;

}
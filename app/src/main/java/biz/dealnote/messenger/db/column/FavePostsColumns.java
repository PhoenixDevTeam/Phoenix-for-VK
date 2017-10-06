package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public final class FavePostsColumns implements BaseColumns {

    private FavePostsColumns(){}

    public static final String TABLENAME = "fave_posts";

    public static final String POST = "post";

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_POST = TABLENAME + "." + POST;

}
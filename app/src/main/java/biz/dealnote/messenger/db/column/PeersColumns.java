package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public final class PeersColumns implements BaseColumns {

    private PeersColumns(){}

    public static final String TABLENAME = "peersnew";

    public static final String UNREAD = "unread";
    public static final String TITLE = "title";
    public static final String IN_READ = "in_read";
    public static final String OUT_READ = "out_read";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String PINNED = "pinned";

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_UNREAD = TABLENAME + "." + UNREAD;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_IN_READ = TABLENAME + "." + IN_READ;
    public static final String FULL_OUT_READ = TABLENAME + "." + OUT_READ;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_PINNED = TABLENAME + "." + PINNED;
}
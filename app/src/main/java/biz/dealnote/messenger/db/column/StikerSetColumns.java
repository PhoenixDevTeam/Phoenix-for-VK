package biz.dealnote.messenger.db.column;

import android.provider.BaseColumns;

public class StikerSetColumns implements BaseColumns {

    public static final String TABLENAME = "sticker_set";
    public static final String TITLE = "title";
    public static final String PHOTO_35 = "photo_35";
    public static final String PHOTO_70 = "photo_70";
    public static final String PHOTO_140 = "photo_140";
    public static final String PURCHASED = "purchased";
    public static final String PROMOTED = "promoted";
    public static final String ACTIVE = "active";
    public static final String STICKERS = "stickers";
    private StikerSetColumns(){}
}

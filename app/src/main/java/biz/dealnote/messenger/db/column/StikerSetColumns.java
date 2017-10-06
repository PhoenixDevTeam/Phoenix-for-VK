package biz.dealnote.messenger.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.util.AssertUtils;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class StikerSetColumns implements BaseColumns {

    private StikerSetColumns(){}

    public static final String TABLENAME = "sticker_set";

    public static final String TITLE = "title";
    public static final String PHOTO_35 = "photo_35";
    public static final String PHOTO_70 = "photo_70";
    public static final String PHOTO_140 = "photo_140";
    //public static final String PHOTO_296 = "photo_296";
    //public static final String PHOTO_592 = "photo_592";
    //public static final String BACKGROUND = "background";
    //public static final String DESCRIPTION = "description";
    public static final String PURCHASED = "purchased";
    public static final String PROMOTED = "promoted";
    public static final String ACTIVE = "active";
    //public static final String TYPE = "type";
    //public static final String BASE_URL = "base_url";
    //public static final String AUTHOR = "author";
    //public static final String FREE = "free";
    //public static final String CAN_PURCHASE = "can_purchase";
    //public static final String PAYMENT_TYPE = "payment_type";
    //public static final String STICKERS_BASE_URL = "stickers_base_url";
    public static final String STICKERS_IDS = "stickers_ids";

    public static ContentValues getCV(@NonNull VKApiStickerSet dto){
        AssertUtils.requireNonNull(dto.product);
        AssertUtils.requireNonNull(dto.product.stickers);

        ContentValues cv = new ContentValues();
        cv.put(PHOTO_35, dto.photo_35);
        cv.put(PHOTO_70, dto.photo_70);
        cv.put(PHOTO_140, dto.photo_140);

        //cv.put(PHOTO_296, dto.photo_296);
        //cv.put(PHOTO_592, dto.photo_592);
        //cv.put(BACKGROUND, dto.background);
        //cv.put(DESCRIPTION, dto.description);
        //cv.put(AUTHOR, dto.author);
        //cv.put(FREE, dto.free);
        //cv.put(CAN_PURCHASE, dto.can_purchase);
        //cv.put(PAYMENT_TYPE, dto.payment_type);

        //if(nonNull(dto.product)){
            VKApiStickerSet.Product product = dto.product;
            cv.put(_ID, product.id);
            cv.put(TITLE, product.title);
            cv.put(PURCHASED, product.purchased);
            cv.put(PROMOTED, product.promoted);
            cv.put(ACTIVE, product.active);
            //cv.put(TYPE, product.type);
            //cv.put(BASE_URL, product.base_url);

            //if(nonNull(product.stickers)){
                VKApiStickerSet.Stickers stickers = product.stickers;
                //cv.put(STICKERS_BASE_URL, stickers.base_url);

                if(nonNull(stickers.sticker_ids)){
                    cv.put(STICKERS_IDS, TextUtils.join(",", stickers.sticker_ids));
                }
            //}
        //}

        return cv;
    }

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_PHOTO_35 = TABLENAME + "." + PHOTO_35;
    public static final String FULL_PHOTO_70 = TABLENAME + "." + PHOTO_70;
    public static final String FULL_PHOTO_140 = TABLENAME + "." + PHOTO_140;
    //public static final String FULL_PHOTO_296 = TABLENAME + "." + PHOTO_296;
    //public static final String FULL_PHOTO_592 = TABLENAME + "." + PHOTO_592;
    //public static final String FULL_BACKGROUND = TABLENAME + "." + BACKGROUND;
    //public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_PURCHASED = TABLENAME + "." + PURCHASED;
    public static final String FULL_PROMOTED = TABLENAME + "." + PROMOTED;
    public static final String FULL_ACTIVE = TABLENAME + "." + ACTIVE;
    //public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    //public static final String FULL_BASE_URL = TABLENAME + "." + BASE_URL;
    //public static final String FULL_AUTHOR = TABLENAME + "." + AUTHOR;
    //public static final String FULL_FREE = TABLENAME + "." +  FREE;
    //public static final String FULL_CAN_PURCHASE = TABLENAME + "." + CAN_PURCHASE;
    //public static final String FULL_PAYMENT_TYPE = TABLENAME + "." + PAYMENT_TYPE;
    //public static final String FULL_STICKERS_BASE_URL = TABLENAME + "." + STICKERS_BASE_URL;
    public static final String FULL_STICKERS_IDS = TABLENAME + "." + STICKERS_IDS;
}

package biz.dealnote.messenger.db.impl;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.StikerSetColumns;
import biz.dealnote.messenger.db.interfaces.IStickersStorage;
import biz.dealnote.messenger.model.StickerSet;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Exestime;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.db.column.StikerSetColumns.ACTIVE;
import static biz.dealnote.messenger.db.column.StikerSetColumns.PHOTO_140;
import static biz.dealnote.messenger.db.column.StikerSetColumns.PHOTO_35;
import static biz.dealnote.messenger.db.column.StikerSetColumns.PHOTO_70;
import static biz.dealnote.messenger.db.column.StikerSetColumns.PROMOTED;
import static biz.dealnote.messenger.db.column.StikerSetColumns.PURCHASED;
import static biz.dealnote.messenger.db.column.StikerSetColumns.STICKERS_IDS;
import static biz.dealnote.messenger.db.column.StikerSetColumns.TITLE;
import static biz.dealnote.messenger.db.column.StikerSetColumns._ID;
import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.join;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class StickersStorage extends AbsStorage implements IStickersStorage {

    StickersStorage(@NonNull AppStorages base) {
        super(base);
    }

    private ContentValues createCv(VKApiStickerSet.Product dto) {
        AssertUtils.requireNonNull(dto);
        AssertUtils.requireNonNull(dto.stickers);

        ContentValues cv = new ContentValues();
        cv.put(PHOTO_35, "https://vk.com/images/store/stickers/" + dto.id + "/cover_35b.png");
        cv.put(PHOTO_70, "https://vk.com/images/store/stickers/" + dto.id + "/cover_70b.png");
        cv.put(PHOTO_140, "https://vk.com/images/store/stickers/" + dto.id + "/cover_140b.png");

        cv.put(_ID, dto.id);
        cv.put(TITLE, dto.title);
        cv.put(PURCHASED, dto.purchased);
        cv.put(PROMOTED, dto.promoted);
        cv.put(ACTIVE, dto.active);

        cv.put(STICKERS_IDS, join(dto.stickers, ",", s -> String.valueOf(s.sticker_id)));
        return cv;
    }

    //{
    //	"response": {
    //		"count": 19,
    //		"items": [{
    //			"id": 279,
    //			"type": "stickers",
    //			"purchased": 1,
    //			"active": 1,
    //			"promoted": 1,
    //			"purchase_date": 0,
    //			"title": "Emoji-стикеры",
    //			"stickers": [{
    //				"sticker_id": 9008,
    //				"is_allowed": true,
    //				"images": [{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-64-1",
    //					"width": 64,
    //					"height": 64
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-128-1",
    //					"width": 128,
    //					"height": 128
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-256-1",
    //					"width": 256,
    //					"height": 256
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-352-1",
    //					"width": 352,
    //					"height": 352
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-512-1",
    //					"width": 512,
    //					"height": 512
    //				}],
    //				"images_with_background": [{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-64b-1",
    //					"width": 64,
    //					"height": 64
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-128b-1",
    //					"width": 128,
    //					"height": 128
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-256b-1",
    //					"width": 256,
    //					"height": 256
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-352b-1",
    //					"width": 352,
    //					"height": 352
    //				},
    //				{
    //					"url": "https:\/\/vk.com\/sticker\/1-9008-512b-1",
    //					"width": 512,
    //					"height": 512
    //				}]
    //			}

    @Override
    public Completable store(int accountId, List<VKApiStickerSet.Product> sets) {
        return Completable.create(e -> {
            long start = System.currentTimeMillis();
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(safeCountOf(sets));

            Uri uri = MessengerContentProvider.getStickerSetContentUriFor(accountId);

            operations.add(ContentProviderOperation
                    .newDelete(uri)
                    .build());

            for (VKApiStickerSet.Product product : sets) {
                if (isNull(product.stickers)) {
                    continue;
                }

                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(createCv(product))
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();

            Exestime.log("StickersStorage.store", start, "count: " + safeCountOf(sets));
        });
    }

    @Override
    public Single<List<StickerSet>> getPurchasedAndActive(int accountId) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();
            Uri uri = MessengerContentProvider.getStickerSetContentUriFor(accountId);

            String where = StikerSetColumns.PURCHASED + " = ? AND " + StikerSetColumns.ACTIVE + " = ?";
            String[] args = new String[]{"1", "1"};
            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            ArrayList<StickerSet> stickers = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    stickers.add(map(cursor));
                }

                cursor.close();
            }

            e.onSuccess(stickers);
            Exestime.log("StickersStorage.get", start, "count: " + stickers.size());
        });
    }

    private static StickerSet map(Cursor cursor) {
        String ids = cursor.getString(cursor.getColumnIndex(StikerSetColumns.STICKERS_IDS));

        StickerSet set = new StickerSet()
                .setPhoto70(cursor.getString(cursor.getColumnIndex(StikerSetColumns.PHOTO_70)));

        if (nonNull(ids)) {
            String[] tokens = ids.split(",");
            for (String token : tokens) {
                set.addId(Integer.parseInt(token));
            }
        }

        return set;
    }
}

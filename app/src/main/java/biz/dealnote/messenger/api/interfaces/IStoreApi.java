package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStoreApi {
    Single<Items<VKApiStickerSet.Product>> getProducts(Boolean extended, String filters, String type);
}
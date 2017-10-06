package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IStoreApi;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.api.services.IStoreService;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class StoreApi extends AbsApi implements IStoreApi {

    StoreApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<VKApiStickerSet>> getStickers() {
        return provideService(IStoreService.class, TokenType.USER)
                .flatMap(service -> service.getStockItems("stickers")
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiStickerSet.Product>> getProducts(Boolean extended, String filters, String type) {
        return provideService(IStoreService.class, TokenType.USER)
                .flatMap(service -> service
                        .getProducts(integerFromBoolean(extended), filters, type)
                        .map(extractResponseWithErrorHandling()));
    }
}
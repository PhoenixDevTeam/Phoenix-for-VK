package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStoreService {

    @FormUrlEncoded
    @POST("store.getStockItems")
    Single<BaseResponse<Items<VKApiStickerSet>>> getStockItems(@Field("type") String type);

    @FormUrlEncoded
    @POST("store.getProducts") //extended=1&filters=active&type=stickers&v=5.64" Thanks for Kate Mobile
    Single<BaseResponse<Items<VKApiStickerSet.Product>>> getProducts(@Field("extended") Integer extended,
                                                                     @Field("filters") String filters,
                                                                     @Field("type") String type);
}
package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.VKApiWikiPage;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IPagesService {

    //https://vk.com/dev/pages.get
    @FormUrlEncoded
    @POST("pages.get")
    Single<BaseResponse<VKApiWikiPage>> get(@Field("owner_id") int ownerId,
                                            @Field("page_id") int pageId,
                                            @Field("global") Integer global,
                                            @Field("site_preview") Integer sitePreview,
                                            @Field("title") String title,
                                            @Field("need_source") Integer needSource,
                                            @Field("need_html") Integer needHtml);

}

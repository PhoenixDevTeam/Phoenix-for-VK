package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.CustomCommentsResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 28.12.2016.
 * phoenix
 */
public interface ICommentsService {

    @FormUrlEncoded
    @POST("execute.getComments")
    Single<BaseResponse<CustomCommentsResponse>> get(@Field("source_type") String sourceType,
                                                     @Field("owner_id") int ownerId,
                                                     @Field("source_id") int sourceId,
                                                     @Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("sort") String sort,
                                                     @Field("start_comment_id") Integer startCommentId,
                                                     @Field("access_key") String accessKey,
                                                     @Field("fields") String fields);
}

package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.NotificationsResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public interface INotificationsService {

    @GET("notifications.markAsViewed")
    Single<BaseResponse<Integer>> markAsViewed();

    @FormUrlEncoded
    @POST("notifications.get")
    Single<BaseResponse<NotificationsResponse>> get(@Field("count") Integer count,
                                                    @Field("start_from") String startFrom,
                                                    @Field("filters") String filters,
                                                    @Field("start_time") Long startTime,
                                                    @Field("end_time") Long endTime);

}

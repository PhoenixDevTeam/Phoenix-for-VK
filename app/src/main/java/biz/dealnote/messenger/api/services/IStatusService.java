package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.response.BaseResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStatusService {

    /**
     * Sets a new status for the current user.
     * @param text Text of the new status.
     * @param groupId Identifier of a community to set a status in. If left blank the status is set to current user.
     * @return 1
     */
    @FormUrlEncoded
    @POST("status.set")
    Single<BaseResponse<Integer>> set(@Field("text") String text,
                                      @Field("group_id") Integer groupId);
}

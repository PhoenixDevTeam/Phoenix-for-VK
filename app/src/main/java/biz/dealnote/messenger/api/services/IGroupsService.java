package biz.dealnote.messenger.api.services;

import java.util.List;

import biz.dealnote.messenger.api.model.GroupSettingsDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.GroupWallInfoResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public interface IGroupsService {

    @FormUrlEncoded
    @POST("groups.editManager")
    Single<BaseResponse<Integer>> editManager(@Field("group_id") int groupId,
                                              @Field("user_id") int userId,
                                              @Field("role") String role,
                                              @Field("is_contact") Integer isContact,
                                              @Field("contact_position") String contactPosition,
                                              @Field("contact_phone") String contactPhone,
                                              @Field("contact_email") String contactEmail);

    @FormUrlEncoded
    @POST("groups.unbanUser")
    Single<BaseResponse<Integer>> unbanUser(@Field("group_id") int groupId,
                                            @Field("user_id") int userId);

    @POST("groups.banUser")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> banUser(@Field("group_id") int groupId,
                                          @Field("user_id") int userId,
                                          @Field("end_date") Long endDate,
                                          @Field("reason") Integer reason,
                                          @Field("comment") String comment,
                                          @Field("comment_visible") Integer commentVisible);

    @FormUrlEncoded
    @POST("groups.getSettings")
    Single<BaseResponse<GroupSettingsDto>> getSettings(@Field("group_id") int groupId);

    //https://vk.com/dev/groups.getBanned
    @FormUrlEncoded
    @POST("groups.getBanned")
    Single<BaseResponse<Items<VKApiUser>>> getBanned(@Field("group_id") int groupId,
                                                     @Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("fields") String fields,
                                                     @Field("user_id") Integer userId);

    @FormUrlEncoded
    @POST("execute.getCommunityWallInfo")
    Single<BaseResponse<GroupWallInfoResponse>> getGroupWallInfo(@Field("group_id") String groupId,
                                                                 @Field("fields") String fields);

    @FormUrlEncoded
    @POST("groups.getMembers")
    Single<BaseResponse<Items<VKApiUser>>> getMembers(@Field("group_id") String groupId,
                                                      @Field("sort") Integer sort,
                                                      @Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("fields") String fields,
                                                      @Field("filter") String filter);

    //https://vk.com/dev/groups.search
    @FormUrlEncoded
    @POST("groups.search")
    Single<BaseResponse<Items<VKApiCommunity>>> search(@Field("q") String query,
                                                       @Field("type") String type,
                                                       @Field("country_id") Integer countryId,
                                                       @Field("city_id") Integer cityId,
                                                       @Field("future") Integer future,
                                                       @Field("market") Integer market,
                                                       @Field("sort") Integer sort,
                                                       @Field("offset") Integer offset,
                                                       @Field("count") Integer count);

    //https://vk.com/dev/groups.leave
    @FormUrlEncoded
    @POST("groups.leave")
    Single<BaseResponse<Integer>> leave(@Field("group_id") int groupId);

    //https://vk.com/dev/groups.join
    @FormUrlEncoded
    @POST("groups.join")
    Single<BaseResponse<Integer>> join(@Field("group_id") int groupId,
                                       @Field("not_sure") Integer notSure);

    //https://vk.com/dev/groups.get
    @FormUrlEncoded
    @POST("groups.get")
    Single<BaseResponse<Items<VKApiCommunity>>> get(@Field("user_id") Integer userId,
                                                    @Field("extended") Integer extended,
                                                    @Field("filter") String filter,
                                                    @Field("fields") String fields,
                                                    @Field("offset") Integer offset,
                                                    @Field("count") Integer count);

    /**
     * Returns information about communities by their IDs.
     *
     * @param groupIds IDs or screen names of communities.
     *                 List of comma-separated words
     * @param groupId  ID or screen name of the community
     * @param fields   Group fields to return. List of comma-separated words
     * @return an array of objects describing communities
     */
    @FormUrlEncoded
    @POST("groups.getById")
    Single<BaseResponse<List<VKApiCommunity>>> getById(@Field("group_ids") String groupIds,
                                                       @Field("group_id") String groupId,
                                                       @Field("fields") String fields);

}

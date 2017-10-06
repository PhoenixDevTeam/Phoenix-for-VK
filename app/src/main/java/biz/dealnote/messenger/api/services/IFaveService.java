package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.FavePostsResponse;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFaveService {

    @FormUrlEncoded
    @POST("fave.getUsers")
    Single<BaseResponse<Items<VKApiUser>>> getUsers(@Field("offset") Integer offset,
                                                    @Field("count") Integer count,
                                                    @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.getPhotos")
    Single<BaseResponse<Items<VKApiPhoto>>> getPhotos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("fave.getVideos")
    Single<BaseResponse<Items<VKApiVideo>>> getVideos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("extended") Integer extended);

    @FormUrlEncoded
    @POST("fave.getPosts")
    Single<BaseResponse<FavePostsResponse>> getPosts(@Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("extended") Integer extended);

    @FormUrlEncoded
    @POST("fave.getLinks")
    Single<BaseResponse<Items<FaveLinkDto>>> getLinks(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("fave.addGroup")
    Single<BaseResponse<Integer>> addGroup(@Field("group_id") int groupId);

    @FormUrlEncoded
    @POST("fave.addUser")
    Single<BaseResponse<Integer>> addUser(@Field("user_id") int userId);

    //https://vk.com/dev/fave.removeUser
    @FormUrlEncoded
    @POST("fave.removeUser")
    Single<BaseResponse<Integer>> removeUser(@Field("user_id") int userId);

    @FormUrlEncoded
    @POST("fave.removeLink")
    Single<BaseResponse<Integer>> removeLink(@Field("link_id") String linkId);

}

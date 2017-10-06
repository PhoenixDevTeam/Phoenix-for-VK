package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class UserWallInfoResponse {

    @SerializedName("user_info")
    public List<VKApiUser> users;

    @SerializedName("all_wall_count")
    public Integer allWallCount;

    @SerializedName("owner_wall_count")
    public Integer ownerWallCount;

    @SerializedName("postponed_wall_count")
    public Integer postponedWallCount;

}

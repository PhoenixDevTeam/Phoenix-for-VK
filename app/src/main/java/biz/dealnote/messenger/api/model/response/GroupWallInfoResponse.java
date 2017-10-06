package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class GroupWallInfoResponse {

    @SerializedName("group_info")
    public List<VKApiCommunity> groups;

    @SerializedName("all_wall_count")
    public Integer allWallCount;

    @SerializedName("owner_wall_count")
    public Integer ownerWallCount;

    @SerializedName("suggests_wall_count")
    public Integer suggestsWallCount;

    @SerializedName("postponed_wall_count")
    public Integer postponedWallCount;
}

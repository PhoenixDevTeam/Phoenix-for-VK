package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 30.04.2017.
 * phoenix
 */
public class WallSearchResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VKApiPost> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}
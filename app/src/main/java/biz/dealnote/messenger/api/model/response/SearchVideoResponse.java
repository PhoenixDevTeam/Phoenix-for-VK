package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public class SearchVideoResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VKApiVideo> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;
}

package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by hp-dv6 on 08.06.2016 with Core i7 2670QM.
 * VKMessenger
 */
public class NewsfeedSearchResponse {

    @SerializedName("items")
    public List<VKApiPost> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    //@SerializedName("count")
    //public Integer count;

    //@SerializedName("total_count")
    //public Integer totalCount;
}

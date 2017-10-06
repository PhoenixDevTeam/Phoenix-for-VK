package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class PostsResponse {

    @SerializedName("items")
    public List<VKApiPost> posts;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

}

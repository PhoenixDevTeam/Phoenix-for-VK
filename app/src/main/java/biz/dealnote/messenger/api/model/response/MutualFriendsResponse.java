package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 30.12.2016.
 * phoenix
 */
public class MutualFriendsResponse {

    @SerializedName("uids")
    public List<Integer> uids;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;
}

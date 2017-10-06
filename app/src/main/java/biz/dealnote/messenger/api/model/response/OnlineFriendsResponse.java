package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class OnlineFriendsResponse {

    @SerializedName("uids")
    public int[] uids;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

}

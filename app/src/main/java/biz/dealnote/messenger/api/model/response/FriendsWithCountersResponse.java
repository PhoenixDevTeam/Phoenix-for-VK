package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class FriendsWithCountersResponse {

    @SerializedName("friends")
    public Items<VKApiUser> friends;

    @SerializedName("counters")
    public VKApiUser.Counters counters;
}

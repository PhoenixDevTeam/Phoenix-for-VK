package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

public final class VkApiBanned {

    @SerializedName("type")
    public String type;

    @SerializedName("profile")
    public VKApiUser profile;

    @SerializedName("group")
    public VKApiCommunity group;

    @SerializedName("ban_info")
    public VKApiUser.BanInfo banInfo;
}
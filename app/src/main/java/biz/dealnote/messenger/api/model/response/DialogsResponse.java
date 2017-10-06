package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VkApiDialog;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class DialogsResponse {

    @SerializedName("items")
    public List<VkApiDialog> dialogs;

    @SerializedName("count")
    public int count;

    @SerializedName("unread_dialogs")
    public int unread;
}

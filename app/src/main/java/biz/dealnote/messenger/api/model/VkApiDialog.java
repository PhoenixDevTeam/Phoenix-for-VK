package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiDialog {

    @SerializedName("conversation")
    public VkApiConversation conversation;

    @SerializedName("last_message")
    public VKApiMessage lastMessage;
}
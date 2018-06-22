package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiDialog {

    @SerializedName("unread")
    public int unread;

    @SerializedName("message")
    public VKApiMessage message;

    @SerializedName("in_read")
    public int in_read;

    @SerializedName("out_read")
    public int out_read;

    public VkApiDialog(){

    }

    public static final class Peer {

        @SerializedName("id")
        public int id;


    }

    public static final class Settings {

        @SerializedName("pinned_message")
        public VKApiMessage pinnedMesage;

        @SerializedName("title")
        public String title;


    }
}
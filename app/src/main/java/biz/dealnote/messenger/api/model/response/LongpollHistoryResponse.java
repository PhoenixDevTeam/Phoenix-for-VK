package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.longpoll.AbsLongpollEvent;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public class LongpollHistoryResponse {

    @SerializedName("new_pts")
    public Long newPts;

    @SerializedName("history")
    public List<AbsLongpollEvent> history;

    @SerializedName("messages")
    public Messages messages;

    public static class Messages {

        @SerializedName("items")
        public List<VKApiMessage> items;

    }

}

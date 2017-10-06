package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
public class BaseResponse<T> extends VkReponse {
    @SerializedName("response")
    public T response;
}
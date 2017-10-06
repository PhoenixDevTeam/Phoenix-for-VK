package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public class RepostReponse {

    @SerializedName("post_id")
    public Integer postId;

    @SerializedName("reposts_count")
    public Integer repostsCount;

    @SerializedName("likes_count")
    public Integer likesCount;

}

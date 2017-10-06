package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public final class CommentsDto {

    @SerializedName("count")
    public int count;

    @SerializedName("can_post")
    public boolean canPost;

    @SerializedName("list")
    public List<VKApiComment> list;
}
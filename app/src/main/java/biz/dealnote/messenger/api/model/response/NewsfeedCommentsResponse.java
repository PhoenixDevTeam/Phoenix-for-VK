package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public class NewsfeedCommentsResponse {

    @SerializedName("items")
    public List<Dto> items;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    public static abstract class Dto {

    }

    public static class PostDto extends Dto {

        public final VKApiPost post;

        public PostDto(VKApiPost post) {
            this.post = post;
        }
    }

    public static class PhotoDto extends Dto {

        public final VKApiPhoto photo;

        public PhotoDto(VKApiPhoto photo) {
            this.photo = photo;
        }
    }

    public static class VideoDto extends Dto {

        public final VKApiVideo video;

        public VideoDto(VKApiVideo video) {
            this.video = video;
        }
    }

    public static class TopicDto extends Dto {

        public final VKApiTopic topic;

        public TopicDto(VKApiTopic topic) {
            this.topic = topic;
        }
    }
}
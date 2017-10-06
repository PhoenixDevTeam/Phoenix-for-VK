package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public class NewsfeedCommentDtoAdapter extends AbsAdapter implements JsonDeserializer<NewsfeedCommentsResponse.Dto> {

    @Override
    public NewsfeedCommentsResponse.Dto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        String type = root.get("type").getAsString();

        NewsfeedCommentsResponse.Dto dto = null;
        if("photo".equals(type)){
            dto = new NewsfeedCommentsResponse.PhotoDto(context.deserialize(root, VKApiPhoto.class));
        } else if("post".equals(type)){
            dto = new NewsfeedCommentsResponse.PostDto(context.deserialize(root, VKApiPost.class));
        } else if("video".equals(type)){
            dto = new NewsfeedCommentsResponse.VideoDto(context.deserialize(root, VKApiVideo.class));
        } else if("topic".equals(type)){
            dto = new NewsfeedCommentsResponse.TopicDto(context.deserialize(root, VKApiTopic.class));
        }

        return dto;
    }
}

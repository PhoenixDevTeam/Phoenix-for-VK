package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.CommentsDto;
import biz.dealnote.messenger.api.model.VKApiTopic;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public class ToticDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiTopic> {
    @Override
    public VKApiTopic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VKApiTopic dto = new VKApiTopic();
        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.title = optString(root, "title");
        dto.created = optLong(root, "created");
        dto.created_by = optInt(root, "created_by");
        dto.updated = optInt(root, "updated");
        dto.updated_by = optInt(root, "updated_by");
        dto.is_closed = optIntAsBoolean(root, "is_closed");
        dto.is_fixed = optIntAsBoolean(root, "is_fixed");

        JsonElement commentsJson = root.get("comments");
        if(nonNull(commentsJson)){
            if(commentsJson.isJsonObject()){
                dto.comments = context.deserialize(commentsJson, CommentsDto.class);
            } else {
                dto.comments = new CommentsDto();
                dto.comments.count = commentsJson.getAsInt();
            }
        }

        dto.first_comment = optString(root, "first_comment");
        dto.last_comment = optString(root, "last_comment");
        return dto;
    }
}
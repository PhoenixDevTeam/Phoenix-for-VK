package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VkApiAttachments;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class CommentDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiComment> {

    @Override
    public VKApiComment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiComment dto = new VKApiComment();

        dto.id = optInt(root, "id");
        dto.from_id = optInt(root, "from_id");

        if(dto.from_id == 0){
            dto.from_id = optInt(root, "owner_id");
        }

        dto.date = optLong(root, "date");
        dto.text = optString(root, "text");
        dto.reply_to_user = optInt(root, "reply_to_user");
        dto.reply_to_comment = optInt(root, "reply_to_comment");

        if(root.has("attachments")){
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if(root.has("likes")){
            JsonObject likesRoot = root.getAsJsonObject("likes");
            dto.likes = optInt(likesRoot, "count");
            dto.user_likes = optIntAsBoolean(likesRoot, "user_likes");
            dto.can_like = optIntAsBoolean(likesRoot, "can_like");
        }

        dto.can_edit = optIntAsBoolean(root, "can_edit");
        return dto;
    }
}
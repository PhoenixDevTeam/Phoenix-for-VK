package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.util.VKStringUtils;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class MessageDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiMessage> {

    @Override
    public VKApiMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiMessage dto = new VKApiMessage();

        dto.id = optInt(root, "id");
        dto.out = optIntAsBoolean(root, "out");
        dto.peer_id = optInt(root, "peer_id");
        dto.from_id = optInt(root, "from_id");

        dto.date = optLong(root, "date");
        dto.read_state = optIntAsBoolean(root, "read_state");
        dto.title = VKStringUtils.unescape(optString(root, "title"));
        dto.body = VKStringUtils.unescape(optString(root, "body"));

        if(root.has("attachments")){
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if(root.has("fwd_messages")){
            JsonArray fwdArray = root.getAsJsonArray("fwd_messages");
            dto.fwd_messages = new ArrayList<>(fwdArray.size());

            for(int i = 0; i < fwdArray.size(); i++){
                dto.fwd_messages.add(deserialize(fwdArray.get(i), VKApiMessage.class, context));
            }
        }

        dto.deleted = optIntAsBoolean(root, "deleted");
        dto.important = optIntAsBoolean(root, "important");

        dto.random_id = optString(root, "random_id");
        dto.payload = optString(root, "payload");
        dto.conversation_message_id = optInt(root, "conversation_message_id");

        JsonObject actionJson = root.getAsJsonObject("action");
        if(actionJson != null){
            dto.action = optString(actionJson, "type");
            dto.action_mid = optInt(actionJson, "member_id");
            dto.action_text = optString(actionJson, "text");
            dto.action_email = optString(actionJson, "email");

            if(actionJson.has("photo")){
                JsonObject photoJson = actionJson.getAsJsonObject("photo");
                dto.action_photo_50 = optString(photoJson, "photo_50");
                dto.action_photo_100 = optString(photoJson, "photo_100");
                dto.action_photo_200 = optString(photoJson, "photo_200");
            }
        }

        return dto;
    }
}
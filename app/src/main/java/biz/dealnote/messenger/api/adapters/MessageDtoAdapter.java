package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import biz.dealnote.messenger.api.model.PushSettings;
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

        int user_id = optInt(root, "user_id");
        int chat_id = optInt(root, "chat_id");

        if (chat_id != 0) {
            dto.peer_id = VKApiMessage.CHAT_PEER + chat_id;
        } else {
            dto.peer_id = user_id;
        }

        if(root.has("from_id")){
            dto.from_id = root.get("from_id").getAsInt();
        } else {
            if (!dto.out) {
                dto.from_id = user_id;
            }
        }

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

        dto.emoji = optIntAsBoolean(root, "emoji");
        dto.deleted = optIntAsBoolean(root, "deleted");
        dto.important = optIntAsBoolean(root, "important");

        if(root.has("chat_active")){
            JsonArray chatActiveArray = root.getAsJsonArray("chat_active");
            int[] chatActiveIds = new int[chatActiveArray.size()];

            for(int i = 0; i < chatActiveArray.size(); i++){
                chatActiveIds[i] = chatActiveArray.get(i).getAsInt();
            }

            dto.chat_active = Arrays.toString(chatActiveIds);
        }

        if(root.has("push_settings")){
            dto.push_settings = context.deserialize(root.get("push_settings"), PushSettings.class);
        }

        dto.users_count = optInt(root, "users_count");
        dto.admin_id = optInt(root, "admin_id");
        dto.action = optString(root, "action");
        dto.action_mid = optInt(root, "action_mid");
        dto.action_email = optString(root, "action_email");
        dto.action_text = optString(root, "action_text");
        dto.photo_50 = optString(root, "photo_50");
        dto.photo_100 = optString(root, "photo_100");
        dto.photo_200 = optString(root, "photo_200");
        dto.random_id = optString(root, "random_id");
        dto.payload = optString(root, "payload");
        return dto;
    }
}
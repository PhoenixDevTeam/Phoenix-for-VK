package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public class ChatUserDtoAdapter extends AbsAdapter implements JsonDeserializer<ChatUserDto> {

    @Override
    public ChatUserDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VKApiUser user = context.deserialize(json, VKApiUser.class);

        ChatUserDto dto = new ChatUserDto();
        JsonObject root = json.getAsJsonObject();

        dto.user = user;
        dto.invited_by = optInt(root, "invited_by");
        dto.type = optString(root, "type");
        return dto;
    }
}

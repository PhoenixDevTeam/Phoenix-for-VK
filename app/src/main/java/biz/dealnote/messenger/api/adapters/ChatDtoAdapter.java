package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiUser;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public class ChatDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiChat> {

    @Override
    public VKApiChat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        VKApiChat dto = new VKApiChat();
        JsonObject root = json.getAsJsonObject();

        dto.id = optInt(root, "id");
        dto.type = optString(root, "type");
        dto.title = optString(root, "title");
        dto.photo_50 = optString(root, "photo_50");
        dto.photo_100 = optString(root, "photo_100");
        dto.photo_200 = optString(root, "photo_200");
        dto.admin_id = optInt(root, "admin_id");

        if(root.has("users")){
            JsonArray users = root.getAsJsonArray("users");
            dto.users = new ArrayList<>(users.size());

            for(int i = 0; i < users.size(); i++){
                JsonElement userElement = users.get(i);

                if(userElement.isJsonPrimitive()){
                    VKApiUser user = new VKApiUser();
                    user.id = userElement.getAsInt();

                    ChatUserDto chatUserDto = new ChatUserDto();
                    chatUserDto.user = user;
                    dto.users.add(chatUserDto);
                } else {
                    JsonObject jsonObject = (JsonObject) userElement;

                    VKApiUser user = context.deserialize(userElement, VKApiUser.class);

                    ChatUserDto chatUserDto = new ChatUserDto();
                    chatUserDto.user = user;
                    chatUserDto.invited_by = optInt(jsonObject, "invited_by", 0);
                    chatUserDto.type = optString(jsonObject, "type");

                    dto.users.add(chatUserDto);
                }
            }
        } else {
            dto.users = new ArrayList<>(0);
        }

        return dto;
    }
}
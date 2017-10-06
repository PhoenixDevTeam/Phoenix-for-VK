package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.response.ChatsInfoResponse;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public class ChatsInfoAdapter extends AbsAdapter implements JsonDeserializer<ChatsInfoResponse> {

    @Override
    public ChatsInfoResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<VKApiChat> chats;

        if(json.isJsonObject()){
            chats = Collections.singletonList(context.deserialize(json, VKApiChat.class));
        } else {
            JsonArray array = json.getAsJsonArray();
            chats = parseArray(array, VKApiChat.class, context, Collections.emptyList());
        }

        ChatsInfoResponse response = new ChatsInfoResponse();
        response.chats = chats;
        return response;
    }
}

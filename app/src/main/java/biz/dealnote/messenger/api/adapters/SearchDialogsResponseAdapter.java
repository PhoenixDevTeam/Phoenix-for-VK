package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.response.SearchDialogsResponse;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class SearchDialogsResponseAdapter extends AbsAdapter implements JsonDeserializer<SearchDialogsResponse> {

    @Override
    public SearchDialogsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();

        SearchDialogsResponse response = new SearchDialogsResponse();

        List<SearchDialogsResponse.AbsChattable> list = new ArrayList<>(array.size());

        for(int i = 0; i < array.size(); i++){
            JsonObject object = array.get(i).getAsJsonObject();
            String type = object.get("type").getAsString();

            if("profile".equals(type)){
                VKApiUser user = context.deserialize(object, VKApiUser.class);
                list.add(new SearchDialogsResponse.User(user));
            } else if("group".equals(type) || "page".equals(type) || "event".equals(type)){
                VKApiCommunity community = context.deserialize(object, VKApiCommunity.class);
                list.add(new SearchDialogsResponse.Community(community));
            } else if("chat".equals(type)){
                VKApiChat chat = context.deserialize(object, VKApiChat.class);
                list.add(new SearchDialogsResponse.Chat(chat));
            }
        }

        response.setData(list);
        return response;
    }
}

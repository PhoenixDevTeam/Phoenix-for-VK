package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.VkApiPrivacy;

/**
 * Created by ruslan.kolbasa on 27.12.2016.
 * phoenix
 */
public class PrivacyDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiPrivacy> {

    @Override
    public VkApiPrivacy deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        // Examples
        // {"category":"only_me"}
        // {"owners":{"allowed":[13326918,26632922,31182820,50949233,113672278,138335672]}}
        VkApiPrivacy privacy = new VkApiPrivacy(optString(root, "category", "only_me"));

        JsonObject owners = root.getAsJsonObject("owners");

        if(owners != null){
            JsonArray allowed = owners.getAsJsonArray("allowed");
            if(allowed != null){
                for(int i = 0; i < allowed.size(); i++){
                    privacy.includeOwner(allowed.get(i).getAsInt());
                }
            }

            JsonArray excluded = owners.getAsJsonArray("excluded");
            if(excluded != null){
                for(int i = 0; i < excluded.size(); i++){
                    privacy.excludeOwner(excluded.get(i).getAsInt());
                }
            }
        }

        JsonObject lists = root.getAsJsonObject("lists");
        if(lists != null){
            JsonArray allowed = lists.getAsJsonArray("allowed");
            if(allowed != null){
                for(int i = 0; i < allowed.size(); i++){
                    privacy.includeFriendsList(allowed.get(i).getAsInt());
                }
            }

            JsonArray excluded = lists.getAsJsonArray("excluded");
            if(excluded != null){
                for(int i = 0; i < excluded.size(); i++){
                    privacy.excludeFriendsList(excluded.get(i).getAsInt());
                }
            }
        }

        return privacy;
    }
}
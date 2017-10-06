package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.response.CustomCommentsResponse;

/**
 * Created by Ruslan Kolbasa on 26.06.2017.
 * phoenix
 */
public class CustomCommentsResponseAdapter implements JsonDeserializer<CustomCommentsResponse> {

    @Override
    public CustomCommentsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        CustomCommentsResponse response = new CustomCommentsResponse();

        if (root.has("main")) {
            JsonElement main = root.get("main");
            if (!main.isJsonPrimitive()) {
                response.main = context.deserialize(main, CustomCommentsResponse.Main.class);
            } // "main": false (if has execute errors)
        }

        if (root.has("first_id")) {
            JsonElement firstIdJson = root.get("first_id");
            response.firstId = firstIdJson.isJsonNull() ? null : firstIdJson.getAsInt();
        }

        if (root.has("last_id")) {
            JsonElement lastIdJson = root.get("last_id");
            response.lastId = lastIdJson.isJsonNull() ? null : lastIdJson.getAsInt();
        }

        if (root.has("admin_level")) {
            JsonElement adminLevelJson = root.get("admin_level");
            response.admin_level = adminLevelJson.isJsonNull() ? null : adminLevelJson.getAsInt();
        }

        return response;
    }
}
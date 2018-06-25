package biz.dealnote.messenger.db.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.model.AttachmentsTypes;
import biz.dealnote.messenger.db.model.entity.EntitiesWrapper;
import biz.dealnote.messenger.db.model.entity.Entity;

/**
 * Created by Ruslan Kolbasa on 21.09.2017.
 * phoenix
 */
public class EntitiesWrapperAdapter implements JsonSerializer<EntitiesWrapper>, JsonDeserializer<EntitiesWrapper> {

    private static final String KEY_NON_NULL = "non_null";
    private static final String KEY_ENTITY = "entity";
    private static final String KEY_TYPE = "type";

    @Override
    public EntitiesWrapper deserialize(JsonElement jsonElement, Type typef, JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement == null || jsonElement instanceof JsonNull) {
            return null;
        }

        JsonArray array = jsonElement.getAsJsonArray();
        List<Entity> entities = new ArrayList<>(array.size());

        for(int i = 0; i < array.size(); i++){
            JsonObject root = array.get(i).getAsJsonObject();

            boolean nonnull = root.get(KEY_NON_NULL).getAsBoolean();

            if (nonnull) {
                int dbotype = root.get(KEY_TYPE).getAsInt();
                entities.add(context.deserialize(root.get(KEY_ENTITY), AttachmentsTypes.classForType(dbotype)));
            } else {
                entities.add(null);
            }
        }

        return new EntitiesWrapper(entities);
    }

    @Override
    public JsonElement serialize(EntitiesWrapper wrapper, Type type, JsonSerializationContext context) {
        if (wrapper == null) {
            return JsonNull.INSTANCE;
        }

        List<Entity> entities = wrapper.get();
        JsonArray array = new JsonArray(entities.size());

        for(Entity entity : entities){
            JsonObject root = new JsonObject();
            root.add(KEY_NON_NULL, new JsonPrimitive(entity != null));

            if (entity != null) {
                root.add(KEY_TYPE, new JsonPrimitive(AttachmentsTypes.typeForInstance(entity)));
                root.add(KEY_ENTITY, context.serialize(entity));
            }

            array.add(root);
        }

        return array;
    }
}
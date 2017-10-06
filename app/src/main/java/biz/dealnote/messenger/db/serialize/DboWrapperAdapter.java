package biz.dealnote.messenger.db.serialize;

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

import biz.dealnote.messenger.db.model.AttachmentsTypes;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

/**
 * Created by Ruslan Kolbasa on 21.09.2017.
 * phoenix
 */
public class DboWrapperAdapter implements JsonSerializer<EntityWrapper>, JsonDeserializer<EntityWrapper> {

    private static final String KEY_IS_NULL = "is_null";
    private static final String KEY_ENTITY = "entity";
    private static final String KEY_TYPE = "type";

    @Override
    public EntityWrapper deserialize(JsonElement jsonElement, Type typef, JsonDeserializationContext context) throws JsonParseException {
        if (jsonElement == null || jsonElement instanceof JsonNull) {
            return null;
        }

        JsonObject root = jsonElement.getAsJsonObject();

        boolean isNull = root.get(KEY_IS_NULL).getAsBoolean();

        Entity entity = null;
        if (!isNull) {
            int dbotype = root.get(KEY_TYPE).getAsInt();
            entity = context.deserialize(root.get(KEY_ENTITY), AttachmentsTypes.classForType(dbotype));
        }

        return new EntityWrapper(entity);
    }

    @Override
    public JsonElement serialize(EntityWrapper wrapper, Type type, JsonSerializationContext context) {
        if (wrapper == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject root = new JsonObject();

        Entity entity = wrapper.get();
        root.add(KEY_IS_NULL, new JsonPrimitive(entity == null));

        if (entity != null) {
            root.add(KEY_TYPE, new JsonPrimitive(AttachmentsTypes.typeForInstance(entity)));
            root.add(KEY_ENTITY, context.serialize(entity));
        }

        return root;
    }
}
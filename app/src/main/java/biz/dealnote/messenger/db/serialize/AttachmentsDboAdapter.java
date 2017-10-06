package biz.dealnote.messenger.db.serialize;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.db.model.AttachmentsTypes;
import biz.dealnote.messenger.db.model.entity.AttachmentsEntity;
import biz.dealnote.messenger.db.model.entity.Entity;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class AttachmentsDboAdapter implements JsonDeserializer<AttachmentsEntity>, JsonSerializer<AttachmentsEntity> {

    private static final String KEY_ENTITY = "entity";
    private static final String KEY_ENTITY_TYPE = "dbo_type";

    @Override
    public AttachmentsEntity deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = jsonElement.getAsJsonArray();

        if(array.size() == 0){
            return new AttachmentsEntity(Collections.emptyList());
        }

        List<Entity> entities = new ArrayList<>(array.size());

        for(int i = 0; i < array.size(); i++){
            JsonObject o = array.get(i).getAsJsonObject();
            int dbotype = o.get(KEY_ENTITY_TYPE).getAsInt();
            entities.add(context.deserialize(o.get(KEY_ENTITY), AttachmentsTypes.classForType(dbotype)));
        }

        return new AttachmentsEntity(entities);
    }

    @Override
    public JsonElement serialize(AttachmentsEntity attachmentsEntity, Type type, JsonSerializationContext context) {
        List<Entity> entities = attachmentsEntity.getEntities();

        JsonArray array = new JsonArray(entities.size());
        for(Entity entity : entities){
            int dbotype = AttachmentsTypes.typeForInstance(entity);

            JsonObject o = new JsonObject();
            o.add(KEY_ENTITY_TYPE, new JsonPrimitive(dbotype));
            o.add(KEY_ENTITY, context.serialize(entity));

            array.add(o);
        }

        return array;
    }
}
package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.database.SchoolClazzDto;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
public class SchoolClazzDtoAdapter extends AbsAdapter implements JsonDeserializer<SchoolClazzDto> {

    @Override
    public SchoolClazzDto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray root = json.getAsJsonArray();
        SchoolClazzDto dto = new SchoolClazzDto();
        dto.id = optInt(root, 0);

        JsonPrimitive second = root.get(1).getAsJsonPrimitive();
        if(second.isString()){
            dto.title = second.getAsString();
        } else if(second.isNumber()){
            dto.title = String.valueOf(second.getAsNumber());
        }

        return dto;
    }
}

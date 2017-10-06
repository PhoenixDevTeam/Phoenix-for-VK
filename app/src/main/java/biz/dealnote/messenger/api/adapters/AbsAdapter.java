package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 24.12.2016.
 * phoenix
 */
public class AbsAdapter {

    public static String optString(JsonObject json, String name){
        return optString(json, name, null);
    }

    public static String optString(JsonObject json, String name, String fallback){
        return json.has(name) ? json.get(name).getAsString() : fallback;
    }

    public static boolean optIntAsBoolean(JsonObject json, String name){
        return optInt(json, name) == 1;
    }

    public static int optInt(JsonObject json, String name){
        return optInt(json, name, 0);
    }

    public static int optInt(JsonArray array, int index){
        return optInt(array, index, 0);
    }

    public static int getFirstInt(JsonObject json, int fallback, String ... names){
        for(String name : names){
            if(json.has(name)){
                return json.get(name).getAsInt();
            }
        }

        return fallback;
    }

    public static long optLong(JsonArray array, int index){
        return optLong(array, index, 0L);
    }

    public static long optLong(JsonArray array, int index, long fallback){
        JsonElement opt = opt(array, index);
        return opt == null ? fallback : opt.getAsLong();
    }

    public static int optInt(JsonArray array, int index, int fallback){
        JsonElement opt = opt(array, index);
        return opt == null ? fallback : opt.getAsInt();
    }

    public static JsonElement opt(JsonArray array, int index){
        if(index < 0 || index >= array.size()){
            return null;
        }

        return array.get(index);
    }

    public static String optString(JsonArray array, int index){
        return optString(array, index, null);
    }

    public static String optString(JsonArray array, int index, String fallback){
        JsonElement opt = opt(array, index);
        return opt == null ? fallback : opt.getAsString();
    }

    public static int optInt(JsonObject json, String name, int def){
        return json.has(name) ? json.get(name).getAsInt() : def;
    }

    public static long optLong(JsonObject json, String name){
        return optLong(json, name, 0L);
    }

    public static long optLong(JsonObject json, String name, long def){
        return json.has(name) ? json.get(name).getAsLong() : def;
    }

    protected static <T> List<T> parseArray(JsonArray array, Class<? extends T> type, JsonDeserializationContext context, List<T> fallback){
        if(array == null){
            return fallback;
        }

        List<T> list = new ArrayList<>();
        for(int i = 0; i < array.size(); i++){
            list.add(context.deserialize(array.get(i), type));
        }

        return list;
    }

    protected static String[] optStringArray(JsonObject root, String name, String[] fallback){
        if(!root.has(name)){
            return fallback;
        }

        JsonArray array = root.getAsJsonArray(name);
        return parseStringArray(array);
    }

    protected static int[] optIntArray(JsonObject root, String name, int[] fallback){
        if(!root.has(name)){
            return fallback;
        }

        JsonArray array = root.getAsJsonArray(name);
        return parseIntArray(array);
    }

    protected static int[] parseIntArray(JsonArray array){
        int[] list = new int[array.size()];
        for(int i = 0; i < array.size(); i++){
            list[i] = array.get(i).getAsInt();
        }

        return list;
    }

    protected static String[] parseStringArray(JsonArray array){
        String[] list = new String[array.size()];
        for(int i = 0; i < array.size(); i++){
            list[i] = array.get(i).getAsString();
        }

        return list;
    }
}
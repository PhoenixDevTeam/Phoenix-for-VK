package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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
        JsonArray array = json.getAsJsonArray();

        VkApiPrivacy privacy = new VkApiPrivacy();

        for (int i = 0; i < array.size(); i++) {
            try {
                int tryUserId = array.get(i).getAsInt();
                boolean exclude = tryUserId < 0;
                int uid = Math.abs(tryUserId);

                privacy.appendEntry(exclude ? VkApiPrivacy.Entry.excludedUser(uid) : VkApiPrivacy.Entry.includedUser(uid));
            } catch (Exception e) {
                String another = optString(array, i);

                if ("all".equals(another)) {
                    privacy.type = VkApiPrivacy.Type.ALL;
                } else if ("friends".equals(another)) {
                    privacy.type = VkApiPrivacy.Type.FRIENDS;
                } else if ("friends_of_friends".equals(another) || "friends_of_friends_only".equals(another)) {
                    privacy.type = VkApiPrivacy.Type.FRIENDS_OF_FRIENDS;
                } else if ("nobody".equals(another) || "only_me".equals(another)) {
                    privacy.type = VkApiPrivacy.Type.ONLY_ME;
                } else if (another.contains("list")) {
                    privacy.appendEntry(parseFriendsList(another));
                }
            }
        }

        // если нет типа, значит никому (пример [only_me, 32271297, 216143660] = [32271297, 216143660])
        if(privacy.type == VkApiPrivacy.Type.UNDEFINED){
            privacy.type = VkApiPrivacy.Type.ONLY_ME;
        }

        return privacy;
    }

    private static VkApiPrivacy.Entry parseFriendsList(String str) {
        boolean exclude = str.startsWith("-");
        int idStart = str.indexOf("list") + 4;
        int id = Integer.parseInt(str.substring(idStart));
        return exclude ? VkApiPrivacy.Entry.excludedFriendsList(id) : VkApiPrivacy.Entry.includedFriendsList(id);
    }
}
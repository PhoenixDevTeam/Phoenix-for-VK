package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import biz.dealnote.messenger.api.model.CommentsDto;
import biz.dealnote.messenger.api.model.PhotoSizeDto;
import biz.dealnote.messenger.api.model.VKApiPhoto;

/**
 * Created by ruslan.kolbasa on 26.12.2016.
 * phoenix
 */
public class PhotoDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiPhoto> {

    @Override
    public VKApiPhoto deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VKApiPhoto photo = new VKApiPhoto();
        photo.id = optInt(root, "id");
        photo.album_id = optInt(root, "album_id");
        photo.date = optLong(root, "date");
        photo.height = optInt(root, "height");
        photo.width = optInt(root, "width");
        photo.owner_id = optInt(root, "owner_id");
        photo.text = optString(root, "text");
        photo.access_key = optString(root, "access_key");

        if (root.has("likes")) {
            JsonObject likesRoot = root.get("likes").getAsJsonObject();
            photo.likes = optInt(likesRoot, "count");
            photo.user_likes = optInt(likesRoot, "user_likes") == 1;
        }

        if (root.has("comments")) {
            JsonElement commentsRoot = root.get("comments").getAsJsonObject();
            photo.comments = context.deserialize(commentsRoot, CommentsDto.class);
        }

        if (root.has("tags")) {
            JsonObject tagsRoot = root.get("tags").getAsJsonObject();
            photo.tags = optInt(tagsRoot, "count");
        }

        photo.can_comment = optInt(root, "can_comment") == 1;
        photo.post_id = optInt(root, "post_id");

        if (root.has("sizes")) {
            JsonArray sizesArray = root.getAsJsonArray("sizes");
            photo.sizes = new ArrayList<>(sizesArray.size());

            for (int i = 0; i < sizesArray.size(); i++) {
                PhotoSizeDto photoSizeDto = context.deserialize(sizesArray.get(i).getAsJsonObject(), PhotoSizeDto.class);
                photo.sizes.add(photoSizeDto);

                if (photo.width != 0 && photo.height != 0) {
                    continue;
                }

                switch (photoSizeDto.type) {
                    case PhotoSizeDto.Type.O:
                    case PhotoSizeDto.Type.P:
                    case PhotoSizeDto.Type.Q:
                    case PhotoSizeDto.Type.R:
                        continue;

                    default:
                        photo.width = photoSizeDto.width;
                        photo.height = photoSizeDto.height;
                        break;
                }
            }
        }

        return photo;
    }
}
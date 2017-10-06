package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.CommentsDto;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.VkApiPrivacy;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class VideoDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiVideo> {

    @Override
    public VKApiVideo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiVideo dto = new VKApiVideo();

        dto.id = optInt(root, "id");
        dto.owner_id = optInt(root, "owner_id");
        dto.title = optString(root, "title");
        dto.description = optString(root, "description");
        dto.duration = optInt(root, "duration");
        dto.link = optString(root, "link");
        dto.date = optLong(root, "date");
        dto.adding_date = optLong(root, "adding_date");
        dto.views = optInt(root, "views");

        JsonElement commentJson = root.get("comments");
        if(nonNull(commentJson)){
            if(commentJson.isJsonObject()){
                //for example, newsfeed.getComment
                dto.comments = context.deserialize(commentJson, CommentsDto.class);
            } else {
                // video.get
                dto.comments = new CommentsDto();
                dto.comments.count = commentJson.getAsInt();
            }
        }

        dto.player = optString(root, "player");
        dto.access_key = optString(root, "access_key");
        dto.album_id = optInt(root, "album_id");

        if(root.has("likes")){
            JsonObject likesRoot = root.getAsJsonObject("likes");
            dto.likes = optInt(likesRoot, "count");
            dto.user_likes = optIntAsBoolean(likesRoot, "user_likes");
        }

        dto.can_comment = optIntAsBoolean(root, "can_comment");
        dto.can_repost = optIntAsBoolean(root, "can_repost");
        dto.repeat = optIntAsBoolean(root, "repeat");

        if(root.has("privacy_view")){
            dto.privacy_view = context.deserialize(root.get("privacy_view"), VkApiPrivacy.class);
        }

        if(root.has("privacy_comment")){
            dto.privacy_comment = context.deserialize(root.get("privacy_comment"), VkApiPrivacy.class);
        }

        if(root.has("files")){
            JsonObject filesRoot = root.getAsJsonObject("files");
            dto.mp4_240 = optString(filesRoot, "mp4_240");
            dto.mp4_360 = optString(filesRoot, "mp4_360");
            dto.mp4_480 = optString(filesRoot, "mp4_480");
            dto.mp4_720 = optString(filesRoot, "mp4_720");
            dto.mp4_1080 = optString(filesRoot, "mp4_1080");
            dto.external = optString(filesRoot, "external");
        }

        dto.photo_130 = optString(root, "photo_130");
        dto.photo_320 = optString(root, "photo_320");
        dto.photo_800 = optString(root, "photo_800");
        dto.platform = optString(root, "platform");

        dto.can_edit = optIntAsBoolean(root, "can_edit");
        dto.can_add = optIntAsBoolean(root, "can_add");
        return dto;
    }
}

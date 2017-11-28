package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPlace;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VkApiAttachments;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public class NewsAdapter extends AbsAdapter implements JsonDeserializer<VKApiNews> {

    @Override
    public VKApiNews deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiNews dto = new VKApiNews();

        dto.type = optString(root, "type");
        dto.source_id = optInt(root, "source_id");
        dto.date = optLong(root, "date");
        dto.post_id = optInt(root, "post_id");
        dto.post_type = optString(root, "post_type");
        dto.final_post = optIntAsBoolean(root, "final_post");
        dto.copy_owner_id = optInt(root, "copy_owner_id");
        dto.copy_post_id = optInt(root, "copy_post_id");

        if(root.has("copy_history")){
            dto.copy_history = parseArray(root.getAsJsonArray("copy_history"), VKApiPost.class, context, null);
        } else {
            dto.copy_history = Collections.emptyList();
        }

        dto.copy_post_date = optLong(root, "copy_post_date");
        dto.text = optString(root, "text");
        dto.can_edit = optIntAsBoolean(root, "can_edit");
        dto.can_delete = optIntAsBoolean(root, "can_delete");

        if(root.has("comments")){
            JsonObject commentsRoot = root.getAsJsonObject("comments");
            dto.comment_count = optInt(commentsRoot, "count");
            dto.comment_can_post = optIntAsBoolean(commentsRoot, "can_post");
        }

        if(root.has("likes")){
            JsonObject likesRoot = root.getAsJsonObject("likes");
            dto.like_count = optInt(likesRoot, "count");
            dto.user_like = optIntAsBoolean(likesRoot, "user_likes");
            dto.can_like = optIntAsBoolean(likesRoot, "can_like");
            dto.can_publish = optIntAsBoolean(likesRoot, "can_publish");
        }

        if(root.has("reposts")){
            JsonObject repostsRoot = root.getAsJsonObject("reposts");
            dto.reposts_count = optInt(repostsRoot, "count");
            dto.user_reposted = optIntAsBoolean(repostsRoot, "user_reposted");
        }

        if(root.has("views")){
            JsonObject viewRoot = root.getAsJsonObject("views");
            dto.views = optInt(viewRoot, "count", 0);
        }

        if(root.has("attachments")){
            dto.attachments = context.deserialize(root.get("attachments"), VkApiAttachments.class);
        }

        if(root.has("geo")){
            dto.geo = context.deserialize(root.get("geo"), VKApiPlace.class);
        }

        if(root.has("photos")){
            JsonArray photosArray = root.getAsJsonObject("photos").getAsJsonArray("items");
            dto.photos = parseArray(photosArray, VKApiPhoto.class, context, null);
        }

        if (root.has("photos_tags")) {
            JsonArray photosTagsArray = root.getAsJsonObject("photos_tags").getAsJsonArray("items");
            dto.photo_tags = parseArray(photosTagsArray, VKApiPhoto.class, context, null);
        }

        //if(root.has("notes")){
        //    dto.notes = parseArray(root.getAsJsonArray("notes"), VKApiNote.class, context, null);
        //}

        if(root.has("friends")){
            JsonArray friendsArray = root.getAsJsonObject("friends").getAsJsonArray("items");
            dto.friends = new ArrayList<>(friendsArray.size());
            for(int i = 0; i < friendsArray.size(); i++){
                JsonObject friendObj = friendsArray.get(i).getAsJsonObject();
                dto.friends.add(friendObj.get("uid").getAsString());
            }
        }

        return dto;
    }
}
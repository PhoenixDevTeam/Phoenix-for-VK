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
import biz.dealnote.messenger.api.model.VKApiPlace;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.VkApiPostSource;

/**
 * Created by admin on 27.12.2016.
 * phoenix
 */
public class PostDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiPost> {

    @Override
    public VKApiPost deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VKApiPost dto = new VKApiPost();

        dto.id = getFirstInt(root, 0, "id", "post_id");
        dto.post_type = VKApiPost.Type.parse(optString(root, "post_type"));
        dto.owner_id = getFirstInt(root, 0, "owner_id", "to_id", "source_id");

        dto.from_id = optInt(root, "from_id");
        dto.date = optLong(root, "date");
        dto.text = optString(root, "text");
        dto.reply_owner_id = optInt(root, "reply_owner_id", 0);

        if (dto.reply_owner_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а!!!
            dto.reply_owner_id = dto.owner_id;
        }

        dto.reply_post_id = optInt(root, "reply_post_id", 0);
        if (dto.reply_post_id == 0) {
            // for replies from newsfeed.search
            // но не помешало бы понять какого хе...а (1)!!!
            dto.reply_post_id = optInt(root, "post_id");
        }

        dto.friends_only = optIntAsBoolean(root, "friends_only");

        if(root.has("comments")){
            JsonObject comments = root.getAsJsonObject("comments");
            dto.comments = context.deserialize(comments, CommentsDto.class);
        }

        if(root.has("likes")){
            JsonObject likes = root.getAsJsonObject("likes");
            dto.likes_count = optInt(likes, "count");
            dto.user_likes = optIntAsBoolean(likes, "user_likes");
            dto.can_like = optIntAsBoolean(likes, "can_like");
            dto.can_publish = optIntAsBoolean(likes, "can_publish");
        }

        if(root.has("reposts")){
            JsonObject reposts = root.getAsJsonObject("reposts");
            dto.reposts_count = optInt(reposts, "count");
            dto.user_reposted = optIntAsBoolean(reposts, "user_reposted");
        }

        if(root.has("views")){
            JsonObject views = root.getAsJsonObject("views");
            dto.views = optInt(views, "count");
        }

        if(root.has("attachments")){
            JsonArray attachments = root.getAsJsonArray("attachments");
            dto.attachments = context.deserialize(attachments, VkApiAttachments.class);
        }

        if(root.has("geo")){
            JsonObject geo = root.getAsJsonObject("geo");
            dto.geo = context.deserialize(geo, VKApiPlace.class);
        }

        dto.can_edit = optIntAsBoolean(root, "can_edit");

        dto.signer_id = optInt(root, "signer_id");
        dto.created_by = optInt(root, "created_by");
        dto.can_pin = optInt(root, "can_pin") == 1;
        dto.is_pinned = optIntAsBoolean(root, "is_pinned");

        if(root.has("copy_history")){
            JsonArray copyHistoryArray = root.getAsJsonArray("copy_history");
            dto.copy_history = new ArrayList<>(copyHistoryArray.size());

            for(int i = 0; i < copyHistoryArray.size(); i++){
                JsonObject copy = copyHistoryArray.get(i).getAsJsonObject();
                dto.copy_history.add(deserialize(copy, VKApiPost.class, context));
            }

        } else {
            //empty list
            dto.copy_history = new ArrayList<>(0);
        }

        if(root.has("post_source")){
            JsonObject postSource = root.getAsJsonObject("post_source");
            dto.post_source = context.deserialize(postSource, VkApiPostSource.class);
        }

        return dto;
    }
}

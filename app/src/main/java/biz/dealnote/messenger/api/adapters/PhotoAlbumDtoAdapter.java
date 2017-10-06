package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import biz.dealnote.messenger.api.model.PhotoSizeDto;
import biz.dealnote.messenger.api.model.VKApiPhotoAlbum;
import biz.dealnote.messenger.api.model.VkApiPrivacy;

/**
 * Created by ruslan.kolbasa on 27.12.2016.
 * phoenix
 */
public class PhotoAlbumDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiPhotoAlbum> {

    @Override
    public VKApiPhotoAlbum deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VKApiPhotoAlbum album = new VKApiPhotoAlbum();

        album.id = optInt(root, "id");
        album.thumb_id = optInt(root, "thumb_id");
        album.owner_id = optInt(root, "owner_id");
        album.title = optString(root, "title");
        album.description = optString(root, "description");
        album.created = optLong(root, "created");
        album.updated = optLong(root, "updated");
        album.size = optInt(root, "size");
        album.can_upload = optInt(root, "can_upload") == 1;
        album.thumb_src = optString(root, "thumb_src");

        if(root.has("privacy_view")){
            album.privacy_view = context.deserialize(root.getAsJsonArray("privacy_view"), VkApiPrivacy.class);
        }

        if(root.has("privacy_comment")){
            album.privacy_comment = context.deserialize(root.getAsJsonArray("privacy_comment"), VkApiPrivacy.class);
        }

        if(root.has("sizes")){
            JsonArray sizesArray = root.getAsJsonArray("sizes");
            album.photo = new ArrayList<>(sizesArray.size());

            for(int i = 0; i < sizesArray.size(); i++){
                album.photo.add(context.deserialize(sizesArray.get(i).getAsJsonObject(), PhotoSizeDto.class));
            }
        } else {
            album.photo = new ArrayList<>(3);
            album.photo.add(PhotoSizeDto.create(PhotoSizeDto.Type.S, "http://vk.com/images/s_noalbum.png"));
            album.photo.add(PhotoSizeDto.create(PhotoSizeDto.Type.M, "http://vk.com/images/m_noalbum.png"));
            album.photo.add(PhotoSizeDto.create(PhotoSizeDto.Type.X, "http://vk.com/images/x_noalbum.png"));
        }

        album.upload_by_admins_only = optInt(root, "upload_by_admins_only") == 1;
        album.comments_disabled = optInt(root, "comments_disabled") == 1;
        return album;
    }
}
package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;

import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiLink;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPoll;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiSticker;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.VKApiWikiPage;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by admin on 27.12.2016.
 * phoenix
 */
public class AttachmentsDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiAttachments> {

    @Override
    public VkApiAttachments deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        VkApiAttachments dto = new VkApiAttachments();

        dto.entries = new ArrayList<>(array.size());
        for(int i = 0; i < array.size(); i++){
            JsonObject o = array.get(i).getAsJsonObject();

            String type = optString(o, "type");
            VKApiAttachment attachment = parse(type, o, context);

            if(Objects.nonNull(attachment)){
                dto.entries.add(new VkApiAttachments.Entry(type, attachment));
            }
        }

        return dto;
    }

    private VKApiAttachment parse(String type, JsonObject root, JsonDeserializationContext context){
        JsonElement o = root.get(type);

        //{"type":"photos_list","photos_list":["406536042_456239026"]}

        if (VkApiAttachments.TYPE_PHOTO.equals(type)) {
            return context.deserialize(o, VKApiPhoto.class);
        } else if (VkApiAttachments.TYPE_VIDEO.equals(type)) {
            return context.deserialize(o, VKApiVideo.class);
        } else if (VkApiAttachments.TYPE_AUDIO.equals(type)) {
            return context.deserialize(o, VKApiAudio.class);
        } else if (VkApiAttachments.TYPE_DOC.equals(type)) {
            return context.deserialize(o, VkApiDoc.class);
        } else if (VkApiAttachments.TYPE_POST.equals(type)) {
            return context.deserialize(o, VKApiPost.class);
        //} else if (VkApiAttachments.TYPE_POSTED_PHOTO.equals(type)) {
        //    return context.deserialize(o, VKApiPostedPhoto.class);
        } else if (VkApiAttachments.TYPE_LINK.equals(type)) {
            return context.deserialize(o, VKApiLink.class);
        //} else if (VkApiAttachments.TYPE_NOTE.equals(type)) {
        //    return context.deserialize(o, VKApiNote.class);
        //} else if (VkApiAttachments.TYPE_APP.equals(type)) {
        //    return context.deserialize(o, VKApiApplicationContent.class);
        } else if (VkApiAttachments.TYPE_POLL.equals(type)) {
            return context.deserialize(o, VKApiPoll.class);
        } else if (VkApiAttachments.TYPE_WIKI_PAGE.equals(type)) {
            return context.deserialize(o, VKApiWikiPage.class);
        //} else if (VkApiAttachments.TYPE_ALBUM.equals(type)) {
        //    return context.deserialize(o, VKApiPhotoAlbum.class); // not supported yet
        } else if (VkApiAttachments.TYPE_STICKER.equals(type)) {
            return context.deserialize(o, VKApiSticker.class);
        }

        return null;
    }
}

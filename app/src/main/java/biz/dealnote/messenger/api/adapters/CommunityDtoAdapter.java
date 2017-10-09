package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Locale;

import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiCity;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiCountry;
import biz.dealnote.messenger.api.model.VKApiPlace;
import biz.dealnote.messenger.api.model.VkApiCover;
import biz.dealnote.messenger.api.util.VKStringUtils;

import static biz.dealnote.messenger.api.model.VKApiCommunity.ACTIVITY;
import static biz.dealnote.messenger.api.model.VKApiCommunity.BAN_INFO;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CAN_CTARE_TOPIC;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CAN_POST;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CAN_SEE_ALL_POSTS;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CAN_UPLOAD_DOC;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CAN_UPLOAD_VIDEO;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CITY;
import static biz.dealnote.messenger.api.model.VKApiCommunity.CONTACTS;
import static biz.dealnote.messenger.api.model.VKApiCommunity.COUNTERS;
import static biz.dealnote.messenger.api.model.VKApiCommunity.COUNTRY;
import static biz.dealnote.messenger.api.model.VKApiCommunity.DESCRIPTION;
import static biz.dealnote.messenger.api.model.VKApiCommunity.FINISH_DATE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.FIXED_POST;
import static biz.dealnote.messenger.api.model.VKApiCommunity.IS_FAVORITE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.LINKS;
import static biz.dealnote.messenger.api.model.VKApiCommunity.MAIN_ALBUM_ID;
import static biz.dealnote.messenger.api.model.VKApiCommunity.MEMBERS_COUNT;
import static biz.dealnote.messenger.api.model.VKApiCommunity.PHOTO_100;
import static biz.dealnote.messenger.api.model.VKApiCommunity.PHOTO_50;
import static biz.dealnote.messenger.api.model.VKApiCommunity.PLACE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.SITE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.START_DATE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.STATUS;
import static biz.dealnote.messenger.api.model.VKApiCommunity.TYPE_EVENT;
import static biz.dealnote.messenger.api.model.VKApiCommunity.TYPE_GROUP;
import static biz.dealnote.messenger.api.model.VKApiCommunity.TYPE_PAGE;
import static biz.dealnote.messenger.api.model.VKApiCommunity.VERIFIED;
import static biz.dealnote.messenger.api.model.VKApiCommunity.WIKI_PAGE;

/**
 * Created by admin on 28.12.2016.
 * phoenix
 */
public class CommunityDtoAdapter extends AbsAdapter implements JsonDeserializer<VKApiCommunity> {

    @Override
    public VKApiCommunity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();
        VKApiCommunity dto = new VKApiCommunity();

        dto.id = optInt(root, "id");
        dto.name = optString(root, "name");
        dto.screen_name = optString(root, "screen_name", String.format(Locale.getDefault(), "club%d", Math.abs(dto.id)));
        dto.is_closed = optInt(root, "is_closed");

        dto.is_admin = optIntAsBoolean(root, "is_admin");
        dto.admin_level = optInt(root, "admin_level");

        dto.is_member = optIntAsBoolean(root, "is_member");
        dto.member_status = optInt(root, "member_status");

        dto.photo_50 = optString(root, "photo_50", PHOTO_50);
        dto.photo_100 = optString(root, "photo_100", PHOTO_100);
        dto.photo_200 = optString(root, "photo_200", null);

        String type = optString(root, "type", "group");
        if(TYPE_GROUP.equals(type)) {
            dto.type = VKApiCommunity.Type.GROUP;
        } else if(TYPE_PAGE.equals(type)) {
            dto.type = VKApiCommunity.Type.PAGE;
        } else if(TYPE_EVENT.equals(type)) {
            dto.type = VKApiCommunity.Type.EVENT;
        }

        if(root.has(CITY)){
            dto.city = context.deserialize(root.get(CITY), VKApiCity.class);
        }

        if(root.has(COUNTRY)){
            dto.country = context.deserialize(root.get(COUNTRY), VKApiCountry.class);
        }

        if(root.has(BAN_INFO)){
            JsonObject banInfo = root.getAsJsonObject(BAN_INFO);
            dto.blacklisted = true;
            dto.ban_end_date = optLong(banInfo, "end_date");
            dto.ban_comment = optString(banInfo, "comment");
        }

        if(root.has(PLACE)){
            dto.place = context.deserialize(root.get(PLACE), VKApiPlace.class);
        }

        dto.description = optString(root, DESCRIPTION);
        dto.wiki_page = optString(root, WIKI_PAGE);
        dto.members_count = optInt(root, MEMBERS_COUNT);

        if(root.has(COUNTERS)){
            JsonElement countersJson = root.get(COUNTERS);

            // because api bug "counters":[]
            if(countersJson.isJsonObject()){
                dto.counters = context.deserialize(countersJson, VKApiCommunity.Counters.class);
            }
        }

        dto.start_date = optLong(root, START_DATE);
        dto.finish_date = optLong(root, FINISH_DATE);
        dto.can_post = optIntAsBoolean(root, CAN_POST);
        dto.can_see_all_posts = optIntAsBoolean(root, CAN_SEE_ALL_POSTS);
        dto.can_upload_doc = optIntAsBoolean(root, CAN_UPLOAD_DOC);
        dto.can_upload_video = optIntAsBoolean(root, CAN_UPLOAD_VIDEO);
        dto.can_create_topic = optIntAsBoolean(root, CAN_CTARE_TOPIC);
        dto.is_favorite = optIntAsBoolean(root, IS_FAVORITE);
        dto.status = VKStringUtils.unescape(optString(root, STATUS));

        if(root.has("status_audio")){
            dto.status_audio = context.deserialize(root.get("status_audio"), VKApiAudio.class);
        }

        dto.contacts = parseArray(root.getAsJsonArray(CONTACTS), VKApiCommunity.Contact.class, context, null);
        dto.links = parseArray(root.getAsJsonArray(LINKS), VKApiCommunity.Link.class, context, null);

        dto.fixed_post = optInt(root, FIXED_POST);
        dto.main_album_id = optInt(root, MAIN_ALBUM_ID);
        dto.verified = optIntAsBoolean(root, VERIFIED);
        dto.site = optString(root, SITE);
        dto.activity = optString(root, ACTIVITY);
        dto.can_message = optIntAsBoolean(root, "can_message");

        if(root.has("cover")){
            dto.cover = context.deserialize(root.get("cover"), VkApiCover.class);
        }

        return dto;
    }
}
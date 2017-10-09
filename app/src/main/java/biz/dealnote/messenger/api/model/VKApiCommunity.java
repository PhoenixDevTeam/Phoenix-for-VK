package biz.dealnote.messenger.api.model;

import android.text.TextUtils;

import java.util.List;

/**
 * Community object describes a community.
 */
public class VKApiCommunity extends VKApiOwner {

    public final static String TYPE_GROUP = "group";
    public final static String TYPE_PAGE = "page";
    public final static String TYPE_EVENT = "event";

    public final static String PHOTO_50 = "http://vk.com/images/community_50.gif";
    public final static String PHOTO_100 = "http://vk.com/images/community_100.gif";

    public static VKApiCommunity create(int id){
        VKApiCommunity community = new VKApiCommunity();
        community.id = id;
        return community;
    }

    /**
     * Community name
     */
    public String name;

    /**
     * Screen name of the community page (e.g. apiclub or club1).
     */
    public String screen_name;

    /**
     * Whether the community is closed
     *
     * @see {@link VKApiCommunity.Status}
     */
    public int is_closed;

    /**
     * Whether a user is the community manager
     */
    public boolean is_admin;

    /**
     * Rights of the user
     *
     * @see {@link VKApiCommunity.AdminLevel}
     */
    public int admin_level;

    /**
     * Whether a user is a community member
     */
    public boolean is_member;

    /**
     * статус участника текущего пользователя в сообществе:
     * 0 — не является участником;
     * 1 — является участником;
     * 2 — не уверен; возможно not_sure опциональный параметр, учитываемый, если group_id принадлежит встрече. 1 — Возможно пойду. 0 — Точно пойду. По умолчанию 0.
     * 3 — отклонил приглашение;
     * 4 — подал заявку на вступление;
     * 5 — приглашен.
     */
    public int member_status;

    public final static String IS_FAVORITE = "is_favorite";

    public final static String MAIN_ALBUM_ID = "main_album_id";

    public final static String CAN_UPLOAD_DOC = "can_upload_doc";

    public final static String CAN_CTARE_TOPIC = "can_upload_video";

    public final static String CAN_UPLOAD_VIDEO = "can_create_topic";

    public final static String BAN_INFO = "ban_info";

    /**
     * Filed city from VK fields set
     */
    public final static String CITY = "city";

    /**
     * Filed country from VK fields set
     */
    public final static String COUNTRY = "country";

    /**
     * Filed place from VK fields set
     */
    public final static String PLACE = "place";

    /**
     * Filed description from VK fields set
     */
    public final static String DESCRIPTION = "description";

    /**
     * Filed wiki_page from VK fields set
     */
    public final static String WIKI_PAGE = "wiki_page";

    /**
     * Filed members_count from VK fields set
     */
    public final static String MEMBERS_COUNT = "members_count";

    /**
     * Filed counters from VK fields set
     */
    public final static String COUNTERS = "counters";

    /**
     * Filed start_date from VK fields set
     */
    public final static String START_DATE = "start_date";

    /**
     * Filed end_date from VK fields set
     */
    public final static String FINISH_DATE = "finish_date";

    /**
     * Filed can_post from VK fields set
     */
    public final static String CAN_POST = "can_post";

    /**
     * Filed can_see_all_posts from VK fields set
     */
    public final static String CAN_SEE_ALL_POSTS = "can_see_all_posts";

    /**
     * Filed status from VK fields set
     */
    public final static String STATUS = "status";

    /**
     * Filed contacts from VK fields set
     */
    public final static String CONTACTS = "contacts";

    /**
     * Filed links from VK fields set
     */
    public final static String LINKS = "links";

    /**
     * Filed fixed_post from VK fields set
     */
    public final static String FIXED_POST = "fixed_post";

    /**
     * Filed verified from VK fields set
     */
    public final static String VERIFIED = "verified";

    /**
     * Filed blacklisted from VK fields set
     */
    public final static String BLACKLISTED = "blacklisted";

    /**
     * Filed site from VK fields set
     */
    public final static String SITE = "site";

    /**
     * Filed activity from VK fields set
     */
    public final static String ACTIVITY = "activity";

    /**
     * City specified in information about community.
     */
    public VKApiCity city;

    /**
     * Country specified in information about community.
     */
    public VKApiCountry country;

    /**
     * Audio which broadcasting to status.
     */
    public VKApiAudio status_audio;

    /**
     * The location which specified in information about community
     */
    public VKApiPlace place;

    /**
     * срок окончания блокировки в формате unixtime;
     */
    public long ban_end_date;

    /**
     * комментарий к блокировке.
     */
    public String ban_comment;

    /**
     * Community description text.
     */
    public String description;

    /**
     * Name of the home wiki-page of the community.
     */
    public String wiki_page;

    /**
     * Number of community members.
     */
    public int members_count;

    /**
     * Counters object with community counters.
     */
    public Counters counters;

    /**
     * Returned only for meeting and contain start time of the meeting as unixtime.
     */
    public long start_date;

    /**
     * Returned only for meeting and contain end time of the meeting as unixtime.
     */
    public long finish_date;

    /**
     * Whether the current user can post on the community's wall
     */
    public boolean can_post;

    /**
     * Whether others' posts on the community's wall can be viewed
     */
    public boolean can_see_all_posts;

    /**
     * Group status.
     */
    public String status;

    /**
     * Information from public page contact module.
     */
    public List<Contact> contacts;

    /**
     * Information from public page links module.
     */
    public List<Link> links;

    /**
     * ID of canDelete post of this community.
     */
    public int fixed_post;

    /**
     * идентификатор основного альбома сообщества.
     */
    public int main_album_id;

    /**
     * Information whether the community has a verified page in VK
     */
    public boolean verified;

    /**
     * информация о том, может ли текущий пользователь загружать документы в группу.
     */
    public boolean can_upload_doc;

    /**
     * информация о том, может ли текущий пользователь загружать видеозаписи в группу
     */
    public boolean can_upload_video;

    /**
     * информация о том, может ли текущий пользователь создать тему обсуждения в группе, используя метод board.addTopic
     */
    public boolean can_create_topic;

    /**
     * возвращается 1, если сообщество находится в закладках у текущего пользователя.
     */
    public boolean is_favorite;


    /**
     * URL of community site
     */
    public String site;

    /**
     * строка состояния публичной страницы. У групп возвращается строковое значение, открыта ли группа или нет, а у событий дата начала.
     */
    public String activity;

    /**
     * Information whether the current community has add current user to the blacklist.
     */
    public boolean blacklisted;

    /**
     * информация о том, может ли текущий пользователь написать сообщение сообществу. Возможные значения:
     */
    public boolean can_message;

    public VkApiCover cover;

    /**
     * VkApiPrivacy status of the group.
     */
    public static class MemberStatus {
        private MemberStatus() {
        }

        public final static int IS_NOT_MEMBER = 0;
        public final static int IS_MEMBER = 1;
        public final static int NOT_SURE = 2;
        public final static int DECLINED_INVITATION = 3;
        public final static int SENT_REQUEST = 4;
        public final static int INVITED = 5;
    }

    /**
     * Community type
     *
     * @see {@link VKApiCommunity.Type}
     */
    public int type;

    /**
     * URL of the 50px-wide community logo.
     */
    public String photo_50;

    /**
     * URL of the 100px-wide community logo.
     */
    public String photo_100;

    /**
     * URL of the 200px-wide community logo.
     */
    public String photo_200;

    /**
     * Creates empty Community instance.
     */
    public VKApiCommunity() {
        super(VKApiOwner.Type.COMMUNITY);
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String getMaxSquareAvatar() {
        if (!TextUtils.isEmpty(photo_200)) {
            return photo_200;
        }

        if (!TextUtils.isEmpty(photo_100)) {
            return photo_100;
        }

        return photo_50;
    }

    /**
     * Access level to manage community.
     */
    public static class AdminLevel {
        private AdminLevel() {
        }

        public final static int MODERATOR = 1;
        public final static int EDITOR = 2;
        public final static int ADMIN = 3;
    }

    /**
     * VkApiPrivacy status of the group.
     */
    public static class Status {
        private Status() {
        }

        public final static int OPEN = 0;
        public final static int CLOSED = 1;
        public final static int PRIVATE = 2;
    }

    /**
     * Types of communities.
     */
    public static class Type {
        private Type() {
        }

        public final static int GROUP = 0;
        public final static int PAGE = 1;
        public final static int EVENT = 2;
    }

    @Override
    public String toString() {
        return "id: " + id + ", name: '" + name + "'";
    }


    public static class Counters {

        /**
         * Значение в том случае, если счетчик не был явно указан.
         */
        public final static int NO_COUNTER = -1;

        public int photos = NO_COUNTER;
        public int albums = NO_COUNTER;
        public int audios = NO_COUNTER;
        public int videos = NO_COUNTER;
        public int topics = NO_COUNTER;
        public int docs = NO_COUNTER;

        public int all_wall = NO_COUNTER;
        public int owner_wall = NO_COUNTER;
        public int suggest_wall = NO_COUNTER;
        public int postponed_wall = NO_COUNTER;

        public Counters() {

        }
    }

    public static final class Contact implements Identificable {

        public int user_id;
        public String email;
        public String phone;
        public String desc;

        public Contact() {

        }

        @Override
        public int getId() {
            return user_id;
        }
    }

    public static class Link {

        public int id;
        public String url;
        public String name;
        public String desc;
        public String photo_50;
        public String photo_100;
        public boolean edit_title;

        public Link() {

        }
    }
}
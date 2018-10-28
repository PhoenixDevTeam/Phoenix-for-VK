package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

public class VkApiConversation {
    @SerializedName("peer")
    public Peer peer;

    /**
     * идентификатор последнего прочтенного входящего сообщения.
     */
    @SerializedName("in_read")
    public int inRead;

    /**
     * идентификатор последнего прочтенного исходящего сообщения.
     */
    @SerializedName("out_read")
    public int outRead;

    @SerializedName("last_message_id")
    public int lastMessageId;

    /**
     * число непрочитанных сообщений.
     */
    @SerializedName("unread_count")
    public int unreadCount;

    /**
     * true, если диалог помечен как важный (только для сообщений сообществ).
     */
    @SerializedName("important")
    public boolean important;

    /**
     * true, если диалог помечен как неотвеченный (только для сообщений сообществ).
     */
    @SerializedName("unanswered")
    public boolean unanswered;

    /**
     * информация о том, может ли пользователь писать в диалог.
     */
    @SerializedName("can_write")
    public CanWrite canWrite;

    @SerializedName("chat_settings")
    public Settings settings;

    public static final class CanWrite {

        /**
         * true, если пользователь может писать в диалог;
         */
        @SerializedName("allowed")
        public boolean allowed;

        /**
         * 18 — пользователь заблокирован или удален;
         900 — нельзя отправить сообщение пользователю, который в чёрном списке;
         901 — пользователь запретил сообщения от сообщества;
         902 — пользователь запретил присылать ему сообщения с помощью настроек приватности;
         915 — в сообществе отключены сообщения;
         916 — в сообществе заблокированы сообщения;
         917 — нет доступа к чату;
         918 — нет доступа к e-mail;
         203 — нет доступа к сообществу.
         */
        @SerializedName("reason")
        public int reason;
    }

    public static final class Peer {

        /**
         * идентификатор назначения.
         */
        @SerializedName("id")
        public int id;

        /**
         * local_id (integer) — локальный идентификатор назначения. Для чатов — id - 2000000000, для сообществ — -id, для e-mail — -(id+2000000000).
         */
        @SerializedName("local_id")
        public int localId;
    }

    public static final class Settings {

        @SerializedName("pinned_message")
        public VKApiMessage pinnedMesage;

        @SerializedName("title")
        public String title;

        @SerializedName("members_count")
        public int membersCount;

        @SerializedName("photo")
        public Photo photo;

        @SerializedName("active_ids")
        public int[] activeIds;

        @SerializedName("state")
        public String state;

        @SerializedName("acl")
        public Acl acl;
    }

    public static final class Acl {
        @SerializedName("can_invite")
        public boolean can_invite;

        @SerializedName("can_change_info")
        public boolean can_change_info;

        @SerializedName("can_change_pin")
        public boolean can_change_pin;

        @SerializedName("can_promote_users")
        public boolean can_promote_users;

        @SerializedName("can_see_invite_link")
        public boolean can_see_invite_link;

        @SerializedName("can_change_invite_link")
        public boolean can_change_invite_link;
    }

    public static final class Photo {

        @SerializedName("photo_50")
        public String photo50;

        @SerializedName("photo_100")
        public String photo100;

        @SerializedName("photo200")
        public String photo200;
    }
}
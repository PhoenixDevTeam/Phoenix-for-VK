package biz.dealnote.messenger.api.model;

import java.util.List;

/**
 * A message object describes a private message
 */
public class VKApiMessage {

    public static final int CHAT_PEER = 2000000000;

    /**
     * 	Message ID. (Not returned for forwarded messages), positive number
     */
    public int id;

    /**
     * For an incoming message, the user ID of the author. For an outgoing message, the user ID of the receiver.
     */
    public int peer_id;

    /**
     * For an incoming message, the user ID of the author. For an outgoing message, the user ID of the receiver.
     */
    public int from_id;

    /**
     * 	Date (in Unix time) when the message was sent.
     */
    public long date;

    /**
     * Message status (false — not read, true — read). (Not returned for forwarded messages.)
     */
    public boolean read_state;

    /**
     * Message type (false — received, true — sent). (Not returned for forwarded messages.)
     */
    public boolean out;

    /**
     * Title of message or chat.
     */
    public String title;

    /**
     * Body of the message.
     */
    public String body;

    /**
     * List of media-attachments;
     */
    public VkApiAttachments attachments;

    /**
     * Array of forwarded messages (if any).
     */
    public List<VKApiMessage> fwd_messages;

    /**
     *	Whether the message contains smiles (false — no, true — yes).
     */
    public boolean emoji;

    /**
     * Whether the message is deleted (false — no, true — yes).
     */
    public boolean important;

    /**
     * Whether the message is deleted (false — no, true — yes).
     */
    public boolean deleted;

    /**
     * 	идентификаторы участников беседы.
     */
    public String chat_active;

    /**
     * 	настройки уведомлений для беседы, если они есть.
     * 	sound и disabled_until
     */
    public PushSettings push_settings;

    /**
     * 	количество участников беседы.
     */
    public int users_count;

    /**
     * идентификатор создателя беседы.
     */
    public int admin_id;

    /**
     * 	поле передано, если это служебное сообщение
     * 	строка, может быть chat_photo_update или chat_photo_remove,
     * 	а с версии 5.14 еще и chat_create, chat_title_update, chat_invite_user, chat_kick_user
     */
    public String action;

    /**
     * 	идентификатор пользователя (если > 0) или email (если < 0), которого пригласили или исключили
     * 	число, для служебных сообщений с action равным chat_invite_user или chat_kick_user
     */
    public int action_mid;

    /**
     * email, который пригласили или исключили
     * строка, для служебных сообщений с action равным chat_invite_user или chat_kick_user и отрицательным action_mid
     */
    public String action_email;

    /**
     * 	название беседы
     * 	строка, для служебных сообщений с action равным chat_create или chat_title_update
     */
    public String action_text;

    /**
     * 	url копии фотографии беседы шириной 50px.
     * 	строка
     */
    public String photo_50;

    /**
     * 	url копии фотографии беседы шириной 100px.
     * 	строка
     */
    public String photo_100;

    /**
     * 	url копии фотографии беседы шириной 200px.
     * 	строка
     */
    public String photo_200;

    /**
     * идентификатор, используемый при отправке сообщения. Возвращается только для исходящих сообщений.
     */
    public String random_id;

    /**
     * is edited?
     */
    public String payload; // "payload":"null"

    /**
     * Creates empty Country instance.
     */
    public VKApiMessage() {

    }

    public static final int FLAG_UNREAD = 1; //сообщение не прочитано
    public static final int FLAG_OUTBOX = 2; //исходящее сообщение
    public static final int FLAG_REPLIED = 4; //на сообщение был создан ответ
    public static final int FLAG_IMPORTANT = 8; //помеченное сообщение
    public static final int FLAG_DIALOG = 16; //сообщение отправлено через диалог
    public static final int FLAG_FRIENDS = 32; //сообщение отправлено другом
    public static final int FLAG_SPAM = 64; //сообщение помечено как "Спам"
    public static final int FLAG_DELETED = 128; //сообщение удалено (в корзине)
    public static final int FLAG_FIXED = 256; //сообщение проверено пользователем на спам
    public static final int FLAG_MEDIA = 512; //сообщение содержит медиаконтент
    public static final int FLAG_GROUP_CHAT = 8192;    //беседа

    public boolean isGroupChat() {
        return peer_id > CHAT_PEER;
    }
}
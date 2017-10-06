package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 04.09.2017.
 * phoenix
 */
public class DialogEntity extends Entity {

    private final int peerId;

    private String title;

    private int unreadCount;

    private String photo50;

    private String photo100;

    private String photo200;

    private MessageEntity message;

    private int adminId;

    private int lastMessageId;

    public DialogEntity(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public String getTitle() {
        return title;
    }

    public DialogEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public DialogEntity setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public DialogEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public DialogEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public DialogEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public MessageEntity getMessage() {
        return message;
    }

    public DialogEntity setMessage(MessageEntity message) {
        this.message = message;
        return this;
    }

    public int getAdminId() {
        return adminId;
    }

    public DialogEntity setAdminId(int adminId) {
        this.adminId = adminId;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public DialogEntity setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }
}
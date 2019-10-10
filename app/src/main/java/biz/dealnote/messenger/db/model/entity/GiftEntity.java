package biz.dealnote.messenger.db.model.entity;

public class GiftEntity extends Entity {
    private int id;
    private int fromId;
    private String message;
    private long date;
    private GiftItemEntity giftItem;
    private int privacy;

    public GiftEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public GiftEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getFromId() {
        return fromId;
    }

    public GiftEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public GiftEntity setMessage(String message) {
        this.message = message;
        return this;
    }

    public long getDate() {
        return date;
    }

    public GiftEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public GiftItemEntity getGiftItem() {
        return giftItem;
    }

    public GiftEntity setGiftItem(GiftItemEntity giftItem) {
        this.giftItem = giftItem;
        return this;
    }

    public int getPrivacy() {
        return privacy;
    }

    public GiftEntity setPrivacy(int privacy) {
        this.privacy = privacy;
        return this;
    }
}

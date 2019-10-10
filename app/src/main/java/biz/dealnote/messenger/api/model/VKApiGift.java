package biz.dealnote.messenger.api.model;

public class VKApiGift {
    public int id;
    public int from_id;
    public String message;
    public long date;
    public VKApiGiftItem giftItem;
    public int privacy;

    public VKApiGift() {
    }
}

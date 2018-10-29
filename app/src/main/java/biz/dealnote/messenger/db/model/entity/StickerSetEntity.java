package biz.dealnote.messenger.db.model.entity;

import java.util.List;

public class StickerSetEntity {

    private final int id;

    private String photo70;

    private String photo35;

    private String photo140;

    private String title;

    private boolean purchased;

    private boolean promoted;

    private boolean active;

    private List<StickerEntity> stickers;

    public StickerSetEntity(int id) {
        this.id = id;
    }

    public String getPhoto70() {
        return photo70;
    }

    public int getId() {
        return id;
    }

    public StickerSetEntity setPhoto70(String photo70) {
        this.photo70 = photo70;
        return this;
    }

    public String getPhoto35() {
        return photo35;
    }

    public StickerSetEntity setPhoto35(String photo35) {
        this.photo35 = photo35;
        return this;
    }

    public String getPhoto140() {
        return photo140;
    }

    public StickerSetEntity setPhoto140(String photo140) {
        this.photo140 = photo140;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public StickerSetEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public StickerSetEntity setPurchased(boolean purchased) {
        this.purchased = purchased;
        return this;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public StickerSetEntity setPromoted(boolean promoted) {
        this.promoted = promoted;
        return this;
    }

    public boolean isActive() {
        return active;
    }

    public StickerSetEntity setActive(boolean active) {
        this.active = active;
        return this;
    }

    public List<StickerEntity> getStickers() {
        return stickers;
    }

    public StickerSetEntity setStickers(List<StickerEntity> stickers) {
        this.stickers = stickers;
        return this;
    }
}
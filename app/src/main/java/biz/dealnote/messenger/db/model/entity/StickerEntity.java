package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class StickerEntity extends Entity {

    private final int id;

    private int width;

    private int height;

    public StickerEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public StickerEntity setHeight(int height) {
        this.height = height;
        return this;
    }

    public StickerEntity setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }
}
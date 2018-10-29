package biz.dealnote.messenger.db.model.entity;

import java.util.List;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class StickerEntity extends Entity {

    private final int id;

    private List<Img> images;

    private List<Img> imagesWithBackground;

    public StickerEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public StickerEntity setImages(List<Img> images) {
        this.images = images;
        return this;
    }

    public StickerEntity setImagesWithBackground(List<Img> imagesWithBackground) {
        this.imagesWithBackground = imagesWithBackground;
        return this;
    }

    public List<Img> getImages() {
        return images;
    }

    public List<Img> getImagesWithBackground() {
        return imagesWithBackground;
    }

    public static final class Img {

        private final String url;
        private final int width;
        private final int height;

        public Img(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public String getUrl() {
            return url;
        }
    }
}
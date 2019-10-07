package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiStickerSet;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public class Sticker extends AbsModel implements Parcelable {

    private final int id;

    private List<Image> images;

    private List<Image> imagesWithBackground;

    private String animationUrl;

    public static final class Image implements Parcelable {

        private final String url;
        private final int width;
        private final int height;

        public Image(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }

        Image(Parcel in) {
            url = in.readString();
            width = in.readInt();
            height = in.readInt();
        }

        public static final Creator<Image> CREATOR = new Creator<Image>() {
            @Override
            public Image createFromParcel(Parcel in) {
                return new Image(in);
            }

            @Override
            public Image[] newArray(int size) {
                return new Image[size];
            }
        };

        public String getUrl() {
            return url;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeInt(width);
            dest.writeInt(height);
        }

        private int calcAverageSize(){
            return (width + height) / 2;
        }
    }

    public Sticker(int id) {
        this.id = id;
    }

    public Image getImage(int prefSize, boolean withBackground){
        return withBackground ? getImage(prefSize, imagesWithBackground) : getImage(prefSize, images);
    }

    private Image getImage(int prefSize, List<Image> images){
        Image result = null;

        for(Image image : images){
            if(result == null){
                result = image;
                continue;
            }

            if(Math.abs(image.calcAverageSize() - prefSize) < Math.abs(result.calcAverageSize() - prefSize)){
                result = image;
            }
        }

        if(result == null){
            // default
            return new Image(VKApiStickerSet.buildImgUrl256(id), 256, 256);
        }

        return result;
    }

    protected Sticker(Parcel in) {
        super(in);
        id = in.readInt();
        images = in.createTypedArrayList(Image.CREATOR);
        imagesWithBackground = in.createTypedArrayList(Image.CREATOR);
        animationUrl = in.readString();
    }

    public String getAnimationUrl() {
        return animationUrl;
    }

    public Sticker setAnimationUrl(String animationUrl) {
        this.animationUrl = animationUrl;
        return this;
    }

    public boolean isAnimated() {
        return animationUrl != null && !animationUrl.isEmpty();
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    public List<Image> getImages() {
        return images;
    }

    public List<Image> getImagesWithBackground() {
        return imagesWithBackground;
    }

    public Sticker setImages(List<Image> images) {
        this.images = images;
        return this;
    }

    public Sticker setImagesWithBackground(List<Image> imagesWithBackground) {
        this.imagesWithBackground = imagesWithBackground;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeTypedList(images);
        dest.writeTypedList(imagesWithBackground);
        dest.writeString(animationUrl);
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
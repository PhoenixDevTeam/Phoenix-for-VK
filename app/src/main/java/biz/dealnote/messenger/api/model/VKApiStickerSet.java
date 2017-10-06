package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VKApiStickerSet implements Identificable {

    @SerializedName("photo_35")
    public String photo_35;

    @SerializedName("photo_70")
    public String photo_70;

    @SerializedName("photo_140")
    public String photo_140;

    @SerializedName("photo_296")
    public String photo_296;

    @SerializedName("photo_592")
    public String photo_592;

    @SerializedName("background")
    public String background;

    @SerializedName("description")
    public String description;

    @SerializedName("author")
    public String author;

    @SerializedName("free")
    public boolean free;

    @SerializedName("can_purchase")
    public boolean can_purchase;

    @SerializedName("payment_type")
    public String payment_type;

    @SerializedName("product")
    public Product product;

    public static String buildImgUrl256(int stickerId) {
        return buildImagUrl(stickerId, 256);
    }

    private static String buildImagUrl(int stickerId, int size) {
        return "https://vk.com/images/stickers/" + stickerId + "/" + size + "b.png";
    }

    public static class Product {

        @SerializedName("id")
        public int id;

        @SerializedName("purchased")
        public boolean purchased;

        @SerializedName("title")
        public String title;

        @SerializedName("promoted")
        public boolean promoted;

        @SerializedName("active")
        public boolean active;

        @SerializedName("type")
        public String type;

        @SerializedName("base_url")
        public String base_url;

        @SerializedName("stickers")
        public Stickers stickers;
    }

    @Override
    public int getId() {
        return product.id;
    }

    public static class Stickers {

        @SerializedName("base_url")
        public String base_url;

        @SerializedName("sticker_ids")
        public List<Integer> sticker_ids;
    }
}
package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public class Sticker extends AbsModel implements Parcelable {

    private final int id;

    private int width;

    private int height;

    private String photo64;

    private String photo128;

    private String photo256;

    public Sticker(int id) {
        this.id = id;
    }

    protected Sticker(Parcel in) {
        super(in);
        id = in.readInt();
        width = in.readInt();
        height = in.readInt();
        photo64 = in.readString();
        photo128 = in.readString();
        photo256 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(photo64);
        dest.writeString(photo128);
        dest.writeString(photo256);
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

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public Sticker setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public Sticker setHeight(int height) {
        this.height = height;
        return this;
    }

    public String getPhoto64() {
        return photo64;
    }

    public Sticker setPhoto64(String photo64) {
        this.photo64 = photo64;
        return this;
    }

    public String getPhoto128() {
        return photo128;
    }

    public Sticker setPhoto128(String photo128) {
        this.photo128 = photo128;
        return this;
    }

    public String getPhoto256() {
        return photo256;
    }

    public Sticker setPhoto256(String photo256) {
        this.photo256 = photo256;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

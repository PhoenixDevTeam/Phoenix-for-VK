package biz.dealnote.messenger.api.model.musicbrainz;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Image implements Parcelable {

    @SerializedName("types")
    public List<String> types;

    @SerializedName("front")
    public boolean front;

    @SerializedName("back")
    public boolean back;

    @SerializedName("edit")
    public long edit;

    @SerializedName("image")
    public String image;

    @SerializedName("comment")
    public String comment;

    @SerializedName("approved")
    public boolean approved;

    @SerializedName("id")
    public String id;

    @SerializedName("thumbnails")
    public Thumbnails thumbnails;

    protected Image(Parcel in) {
        this.types = in.createStringArrayList();
        this.front = in.readByte() != 0;
        this.back = in.readByte() != 0;
        this.edit = in.readLong();
        this.image = in.readString();
        this.comment = in.readString();
        this.approved = in.readByte() != 0;
        this.id = in.readString();
        this.thumbnails = in.readParcelable(Thumbnails.class.getClassLoader());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(types);
        dest.writeByte((byte) (front ? 1 : 0));
        dest.writeByte((byte) (back ? 1 : 0));
        dest.writeLong(edit);
        dest.writeString(image);
        dest.writeString(comment);
        dest.writeByte((byte) (approved ? 1 : 0));
        dest.writeString(id);
        dest.writeParcelable(thumbnails, flags);
    }
}

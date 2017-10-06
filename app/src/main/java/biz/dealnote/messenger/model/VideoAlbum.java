package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideoAlbum extends AbsModel implements Parcelable {

    private final int id;

    private final int ownerId;

    private String title;

    private int count;

    private long updatedTime;

    private String photo160;

    private String photo320;

    private SimplePrivacy privacy;

    public VideoAlbum(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected VideoAlbum(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        title = in.readString();
        count = in.readInt();
        updatedTime = in.readLong();
        photo160 = in.readString();
        photo320 = in.readString();
        privacy = in.readParcelable(SimplePrivacy.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeString(title);
        dest.writeInt(count);
        dest.writeLong(updatedTime);
        dest.writeString(photo160);
        dest.writeString(photo320);
        dest.writeParcelable(privacy, flags);
    }

    public static final Creator<VideoAlbum> CREATOR = new Creator<VideoAlbum>() {
        @Override
        public VideoAlbum createFromParcel(Parcel in) {
            return new VideoAlbum(in);
        }

        @Override
        public VideoAlbum[] newArray(int size) {
            return new VideoAlbum[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public VideoAlbum setTitle(String title) {
        this.title = title;
        return this;
    }

    public VideoAlbum setPrivacy(SimplePrivacy privacy) {
        this.privacy = privacy;
        return this;
    }

    public SimplePrivacy getPrivacy() {
        return privacy;
    }

    public int getCount() {
        return count;
    }

    public VideoAlbum setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public VideoAlbum setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public String getPhoto160() {
        return photo160;
    }

    public VideoAlbum setPhoto160(String photo160) {
        this.photo160 = photo160;
        return this;
    }

    public String getPhoto320() {
        return photo320;
    }

    public VideoAlbum setPhoto320(String photo320) {
        this.photo320 = photo320;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

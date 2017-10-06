package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;

public class Chat extends AbsModel implements Parcelable {

    private final int id;

    private String title;

    private String photo50;

    private String photo100;

    private String photo200;

    private int adminId;

    public Chat(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Chat setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Chat setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Chat setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Chat setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public int getAdminId() {
        return adminId;
    }

    public Chat setAdminId(int adminId) {
        this.adminId = adminId;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeInt(adminId);
    }

    public Chat(Parcel in) {
        super(in);
        this.id = in.readInt();
        this.title = in.readString();
        this.photo50 = in.readString();
        this.photo100 = in.readString();
        this.photo200 = in.readString();
        this.adminId = in.readInt();
    }

    public static Parcelable.Creator<Chat> CREATOR = new Parcelable.Creator<Chat>() {
        public Chat createFromParcel(Parcel source) {
            return new Chat(source);
        }

        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String get100orSmallerAvatar(){
        return firstNonEmptyString(photo100, photo50);
    }

    public String getMaxSquareAvatar(){
        return firstNonEmptyString(photo200, photo100, photo200);
    }
}

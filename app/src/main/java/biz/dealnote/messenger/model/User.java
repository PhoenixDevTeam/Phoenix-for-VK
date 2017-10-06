package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.api.model.Identificable;

import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
public class User extends Owner implements Parcelable, Identificable {

    private final int id;

    private String firstName;

    private String lastName;

    private boolean online;

    private boolean onlineMobile;

    private int onlineApp;

    private String photo50;

    private String photo100;

    private String photo200;

    private long lastSeen;

    @UserPlatform
    private int platform;

    private String status;

    @Sex
    private int sex;

    private String domain;

    private boolean friend;

    private int friendStatus;

    public User(int id) {
        super(OwnerType.USER);
        this.id = id;
    }

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public boolean isOnline() {
        return online;
    }

    public User setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public User setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
        return this;
    }

    public int getOnlineApp() {
        return onlineApp;
    }

    public User setOnlineApp(int onlineApp) {
        this.onlineApp = onlineApp;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public User setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public User setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public User setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public User setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    @UserPlatform
    public int getPlatform() {
        return platform;
    }

    public User setPlatform(@UserPlatform int platform) {
        this.platform = platform;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public User setStatus(String status) {
        this.status = status;
        return this;
    }

    @Sex
    public int getSex() {
        return sex;
    }

    public User setSex(@Sex int sex) {
        this.sex = sex;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public User setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public boolean isFriend() {
        return friend;
    }

    public User setFriend(boolean friend) {
        this.friend = friend;
        return this;
    }

    public User setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
        return this;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    protected User(Parcel in) {
        super(in);
        id = in.readInt();
        firstName = in.readString();
        lastName = in.readString();
        online = in.readByte() != 0;
        onlineMobile = in.readByte() != 0;
        onlineApp = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        lastSeen = in.readLong();
        //noinspection ResourceType
        platform = in.readInt();
        status = in.readString();
        //noinspection ResourceType
        sex = in.readInt();
        domain = in.readString();
        friend = in.readByte() != 0;
        friendStatus = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (onlineMobile ? 1 : 0));
        dest.writeInt(onlineApp);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeLong(lastSeen);
        dest.writeInt(platform);
        dest.writeString(status);
        dest.writeInt(sex);
        dest.writeString(domain);
        dest.writeByte((byte) (friend ? 1 : 0));
        dest.writeInt(friendStatus);
    }

    @Override
    public int getOwnerId() {
        return Math.abs(this.id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getId() {
        return id;
    }

    @Override
    public String get100photoOrSmaller() {
        return firstNonEmptyString(photo100, photo50);
    }

    @Override
    public String getMaxSquareAvatar() {
        return firstNonEmptyString(photo200, photo100, photo50);
    }
}
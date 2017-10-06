package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class UserEntity {

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

    private int platform;

    private String status;

    private int sex;

    private String domain;

    private boolean friend;
    private int friendStatus;

    public UserEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserEntity setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public UserEntity setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public boolean isOnline() {
        return online;
    }

    public UserEntity setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public UserEntity setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
        return this;
    }

    public int getOnlineApp() {
        return onlineApp;
    }

    public UserEntity setOnlineApp(int onlineApp) {
        this.onlineApp = onlineApp;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public UserEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public UserEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public UserEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public UserEntity setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public int getPlatform() {
        return platform;
    }

    public UserEntity setPlatform(int platform) {
        this.platform = platform;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public int getSex() {
        return sex;
    }

    public UserEntity setSex(int sex) {
        this.sex = sex;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public UserEntity setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public boolean isFriend() {
        return friend;
    }

    public UserEntity setFriend(boolean friend) {
        this.friend = friend;
        return this;
    }

    public UserEntity setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
        return this;
    }

    public int getFriendStatus() {
        return friendStatus;
    }
}
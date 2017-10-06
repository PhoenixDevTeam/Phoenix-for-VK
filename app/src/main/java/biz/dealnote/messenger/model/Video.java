package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.util.Utils;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class Video extends AbsModel implements Parcelable {

    private int id;

    private int ownerId;

    private int albumId;

    private String title;

    private String description;

    private String link;

    private long date;

    private long addingDate;

    private int views;

    private String player;

    private String photo130;

    private String photo320;

    private String photo800;

    private String accessKey;

    private int commentsCount;

    private boolean userLikes;

    private int likesCount;

    private String mp4link240;

    private String mp4link360;

    private String mp4link480;

    private String mp4link720;

    private String mp4link1080;

    private String externalLink;

    private String platform;

    private boolean repeat;

    private int duration;

    private SimplePrivacy privacyView;

    private SimplePrivacy privacyComment;

    private boolean canEdit;

    private boolean canAdd;

    private boolean canComment;

    private boolean canRepost;

    public static final int SIZE_240 = 240;
    public static final int SIZE_360 = 360;
    public static final int SIZE_480 = 480;
    public static final int SIZE_720 = 720;
    public static final int SIZE_1080 = 1080;

    public Video(){

    }

    protected Video(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        albumId = in.readInt();
        title = in.readString();
        description = in.readString();
        link = in.readString();
        date = in.readLong();
        addingDate = in.readLong();
        views = in.readInt();
        player = in.readString();
        photo130 = in.readString();
        photo320 = in.readString();
        photo800 = in.readString();
        accessKey = in.readString();
        commentsCount = in.readInt();
        canComment = in.readByte() != 0;
        canRepost = in.readByte() != 0;
        userLikes = in.readByte() != 0;
        likesCount = in.readInt();
        mp4link240 = in.readString();
        mp4link360 = in.readString();
        mp4link480 = in.readString();
        mp4link720 = in.readString();
        mp4link1080 = in.readString();
        externalLink = in.readString();
        platform = in.readString();
        repeat = in.readByte() != 0;
        duration = in.readInt();
        privacyView = in.readParcelable(SimplePrivacy.class.getClassLoader());
        privacyComment = in.readParcelable(SimplePrivacy.class.getClassLoader());
        canEdit = in.readByte() != 0;
        canAdd = in.readByte() != 0;
    }

    public SimplePrivacy getPrivacyView() {
        return privacyView;
    }

    public Video setPrivacyView(SimplePrivacy privacyView) {
        this.privacyView = privacyView;
        return this;
    }

    public SimplePrivacy getPrivacyComment() {
        return privacyComment;
    }

    public Video setPrivacyComment(SimplePrivacy privacyComment) {
        this.privacyComment = privacyComment;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeInt(albumId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeLong(date);
        dest.writeLong(addingDate);
        dest.writeInt(views);
        dest.writeString(player);
        dest.writeString(photo130);
        dest.writeString(photo320);
        dest.writeString(photo800);
        dest.writeString(accessKey);
        dest.writeInt(commentsCount);
        dest.writeByte((byte) (canComment ? 1 : 0));
        dest.writeByte((byte) (canRepost ? 1 : 0));
        dest.writeByte((byte) (userLikes ? 1 : 0));
        dest.writeInt(likesCount);
        dest.writeString(mp4link240);
        dest.writeString(mp4link360);
        dest.writeString(mp4link480);
        dest.writeString(mp4link720);
        dest.writeString(mp4link1080);
        dest.writeString(externalLink);
        dest.writeString(platform);
        dest.writeByte((byte) (repeat ? 1 : 0));
        dest.writeInt(duration);
        dest.writeParcelable(privacyView, flags);
        dest.writeParcelable(privacyComment, flags);
        dest.writeByte((byte) (canEdit ? 1 : 0));
        dest.writeByte((byte) (canAdd ? 1 : 0));
    }

    public Video setCanAdd(boolean canAdd) {
        this.canAdd = canAdd;
        return this;
    }

    public Video setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanAdd() {
        return canAdd;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    public int getId() {
        return id;
    }

    public Video setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Video setOwnerId(int ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public Video setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Video setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Video setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Video setLink(String link) {
        this.link = link;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Video setDate(long date) {
        this.date = date;
        return this;
    }

    public long getAddingDate() {
        return addingDate;
    }

    public Video setAddingDate(long addingDate) {
        this.addingDate = addingDate;
        return this;
    }

    public int getViews() {
        return views;
    }

    public Video setViews(int views) {
        this.views = views;
        return this;
    }

    public String getPlayer() {
        return player;
    }

    public Video setPlayer(String player) {
        this.player = player;
        return this;
    }

    public String getPhoto130() {
        return photo130;
    }

    public Video setPhoto130(String photo130) {
        this.photo130 = photo130;
        return this;
    }

    public String getPhoto320() {
        return photo320;
    }

    public Video setPhoto320(String photo320) {
        this.photo320 = photo320;
        return this;
    }

    public String getPhoto800() {
        return photo800;
    }

    public Video setPhoto800(String photo800) {
        this.photo800 = photo800;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Video setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public Video setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public boolean isCanComment() {
        return canComment;
    }

    public Video setCanComment(boolean canComment) {
        this.canComment = canComment;
        return this;
    }

    public boolean isCanRepost() {
        return canRepost;
    }

    public Video setCanRepost(boolean canRepost) {
        this.canRepost = canRepost;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public Video setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public Video setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public String getMp4link240() {
        return mp4link240;
    }

    public Video setMp4link240(String mp4link240) {
        this.mp4link240 = mp4link240;
        return this;
    }

    public String getMp4link360() {
        return mp4link360;
    }

    public Video setMp4link360(String mp4link360) {
        this.mp4link360 = mp4link360;
        return this;
    }

    public String getMp4link480() {
        return mp4link480;
    }

    public Video setMp4link480(String mp4link480) {
        this.mp4link480 = mp4link480;
        return this;
    }

    public String getMp4link720() {
        return mp4link720;
    }

    public Video setMp4link720(String mp4link720) {
        this.mp4link720 = mp4link720;
        return this;
    }

    public String getMp4link1080() {
        return mp4link1080;
    }

    public Video setMp4link1080(String mp4link1080) {
        this.mp4link1080 = mp4link1080;
        return this;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public Video setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public Video setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public Video setRepeat(boolean repeat) {
        this.repeat = repeat;
        return this;
    }

    public Video setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public String get320orSmallerPhoto() {
        return Utils.firstNonEmptyString(photo320, photo130);
    }

    public String getMaxResolutionPhoto() {
        return Utils.firstNonEmptyString(photo800, photo320, photo130);
    }
}

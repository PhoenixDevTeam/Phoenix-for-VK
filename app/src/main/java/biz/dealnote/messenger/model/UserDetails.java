package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
public final class UserDetails implements Parcelable {

    private IdPair photoId;

    private Audio statusAudio;

    private int friendsCount;

    private int onlineFriendsCount;

    private int mutualFriendsCount;

    private int followersCount;

    private int groupsCount;

    private int photosCount;

    private int audiosCount;

    private int videosCount;

    private int allWallCount;

    private int ownWallCount;

    private int postponedWallCount;

    public UserDetails(){

    }

    private UserDetails(Parcel in) {
        photoId = in.readParcelable(IdPair.class.getClassLoader());
        statusAudio = in.readParcelable(Audio.class.getClassLoader());
        friendsCount = in.readInt();
        onlineFriendsCount = in.readInt();
        mutualFriendsCount = in.readInt();
        followersCount = in.readInt();
        groupsCount = in.readInt();
        photosCount = in.readInt();
        audiosCount = in.readInt();
        videosCount = in.readInt();
        allWallCount = in.readInt();
        ownWallCount = in.readInt();
        postponedWallCount = in.readInt();
    }

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel in) {
            return new UserDetails(in);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(photoId, i);
        parcel.writeParcelable(statusAudio, i);
        parcel.writeInt(friendsCount);
        parcel.writeInt(onlineFriendsCount);
        parcel.writeInt(mutualFriendsCount);
        parcel.writeInt(followersCount);
        parcel.writeInt(groupsCount);
        parcel.writeInt(photosCount);
        parcel.writeInt(audiosCount);
        parcel.writeInt(videosCount);
        parcel.writeInt(allWallCount);
        parcel.writeInt(ownWallCount);
        parcel.writeInt(postponedWallCount);
    }

    public IdPair getPhotoId() {
        return photoId;
    }

    public UserDetails setPhotoId(IdPair photoId) {
        this.photoId = photoId;
        return this;
    }

    public Audio getStatusAudio() {
        return statusAudio;
    }

    public UserDetails setStatusAudio(Audio statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public UserDetails setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public int getOnlineFriendsCount() {
        return onlineFriendsCount;
    }

    public UserDetails setOnlineFriendsCount(int onlineFriendsCount) {
        this.onlineFriendsCount = onlineFriendsCount;
        return this;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public UserDetails setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public UserDetails setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public UserDetails setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public UserDetails setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public UserDetails setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public UserDetails setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public UserDetails setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnWallCount() {
        return ownWallCount;
    }

    public UserDetails setOwnWallCount(int ownWallCount) {
        this.ownWallCount = ownWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public UserDetails setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }
}
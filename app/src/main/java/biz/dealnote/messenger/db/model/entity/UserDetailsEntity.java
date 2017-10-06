package biz.dealnote.messenger.db.model.entity;

import biz.dealnote.messenger.db.model.IdPairEntity;

/**
 * Created by admin on 17.09.2017.
 * phoenix
 */
public class UserDetailsEntity {

    private IdPairEntity photoId;

    private AudioEntity statusAudio;

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

    public IdPairEntity getPhotoId() {
        return photoId;
    }

    public UserDetailsEntity setPhotoId(IdPairEntity photoId) {
        this.photoId = photoId;
        return this;
    }

    public AudioEntity getStatusAudio() {
        return statusAudio;
    }

    public UserDetailsEntity setStatusAudio(AudioEntity statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public UserDetailsEntity setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public int getOnlineFriendsCount() {
        return onlineFriendsCount;
    }

    public UserDetailsEntity setOnlineFriendsCount(int onlineFriendsCount) {
        this.onlineFriendsCount = onlineFriendsCount;
        return this;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public UserDetailsEntity setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public UserDetailsEntity setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public UserDetailsEntity setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public UserDetailsEntity setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public UserDetailsEntity setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public UserDetailsEntity setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public UserDetailsEntity setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnWallCount() {
        return ownWallCount;
    }

    public UserDetailsEntity setOwnWallCount(int ownWallCount) {
        this.ownWallCount = ownWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public UserDetailsEntity setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }
}
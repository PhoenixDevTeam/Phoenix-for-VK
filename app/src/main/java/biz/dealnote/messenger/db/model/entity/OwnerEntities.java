package biz.dealnote.messenger.db.model.entity;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class OwnerEntities {

    private final List<UserEntity> userEntities;

    private final List<CommunityEntity> communityEntities;

    public OwnerEntities(@NonNull List<UserEntity> userEntities, @NonNull List<CommunityEntity> communityEntities) {
        this.userEntities = userEntities;
        this.communityEntities = communityEntities;
    }

    public List<CommunityEntity> getCommunityEntities() {
        return communityEntities;
    }

    public List<UserEntity> getUserEntities() {
        return userEntities;
    }

    public int size(){
        return userEntities.size() + communityEntities.size();
    }
}
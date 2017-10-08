package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.db.model.entity.CommunityEntity;
import biz.dealnote.messenger.db.model.entity.FriendListEntity;
import biz.dealnote.messenger.db.model.entity.UserEntity;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IRelativeshipStore extends IStore {

    Completable storeFriendsList(int accountId, int userId, @NonNull Collection<FriendListEntity> data);

    Completable storeFriends(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore);
    Completable storeFollowers(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore);

    Single<List<UserEntity>> getFriends(int accountId, int objectId);
    Single<List<UserEntity>> getFollowers(int accountId, int objectId);

    Single<List<CommunityEntity>> getCommunities(int accountId, int ownerId);

    Completable storeComminities(int accountId, List<CommunityEntity> communities, int userId, boolean invalidateBefore);
}
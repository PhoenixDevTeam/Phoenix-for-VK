package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public interface IRelationshipInteractor {
    Single<List<User>> getCachedFriends(int accountId, int objectId);
    Single<List<User>> getCachedFollowers(int accountId, int objectId);
    Single<List<User>> getActualFriendsList(int accountId, int objectId, int count, int offset);
    Single<List<User>> getOnlineFriends(int accountId, int objectId, int count, int offset);
    Single<List<User>> getFollowers(int accountId, int objectId, int count, int offset);
    Single<List<User>> getMutualFriends(int accountId, int objectId, int count, int offset);

    Single<Pair<List<User>, Integer>> seacrhFriends(int accountId, int userId, int count, int offset, String q);

    Single<FriendsCounters> getFriendsCounters(int accountId, int userId);

    Single<Integer> addFriend(int accountId, int userId, String optionalText, boolean keepFollow);

    Single<Integer> deleteFriends(int accountId, int userId);

    int FRIEND_ADD_REQUEST_SENT = 1;
    int FRIEND_ADD_REQUEST_FROM_USER_APPROVED = 2;
    int FRIEND_ADD_RESENDING = 4;

    interface DeletedCodes {
        int FRIEND_DELETED = 1;
        int OUT_REQUEST_DELETED = 2;
        int IN_REQUEST_DELETED = 3;
        int SUGGESTION_DELETED = 4;
    }
}
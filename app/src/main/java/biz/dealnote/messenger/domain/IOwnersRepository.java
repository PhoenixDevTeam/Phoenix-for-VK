package biz.dealnote.messenger.domain;

import java.util.Collection;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import biz.dealnote.messenger.api.model.longpoll.UserIsOfflineUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOnlineUpdate;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.CommunityDetails;
import biz.dealnote.messenger.model.IOwnersBundle;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserDetails;
import biz.dealnote.messenger.model.UserUpdate;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Created by admin on 03.02.2017.
 * phoenix
 */
public interface IOwnersRepository {

    int MODE_ANY = 1;

    int MODE_NET = 2;

    int MODE_CACHE = 3;

    Single<List<Owner>> findBaseOwnersDataAsList(int accountId, Collection<Integer> ids, int mode);

    Single<IOwnersBundle> findBaseOwnersDataAsBundle(int accountId, Collection<Integer> ids, int mode);

    Single<IOwnersBundle> findBaseOwnersDataAsBundle(int accountId, Collection<Integer> ids, int mode, Collection<? extends Owner> alreadyExists);

    Single<Owner> getBaseOwnerInfo(int accountId, int ownerId, int mode);

    Single<Pair<User, UserDetails>> getFullUserInfo(int accountId, int userId, int mode);

    Single<Pair<Community, CommunityDetails>> getFullCommunityInfo(int accountId, int comminityId, int mode);

    Completable cacheActualOwnersData(int accountId, Collection<Integer> ids);

    Single<List<Owner>> getCommunitiesWhereAdmin(int accountId, boolean admin, boolean editor, boolean moderator);

    Single<List<User>> searchPeoples(int accountId, PeopleSearchCriteria criteria, int count, int offset);

    Completable insertOwners(int accountId, @NonNull OwnerEntities entities);

    Completable handleStatusChange(int accountId, int userId, String status);

    Completable handleOnlineChanges(int accountId, @Nullable List<UserIsOfflineUpdate> offlineUpdates, @Nullable List<UserIsOnlineUpdate> onlineUpdates);

    Flowable<List<UserUpdate>> observeUpdates();
}
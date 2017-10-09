package biz.dealnote.messenger.domain.impl;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VkApiFriendList;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.model.entity.FriendListEntity;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.IUtilsInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.FriendList;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public class UtilsInteractor implements IUtilsInteractor {

    private final INetworker networker;
    private final IStores stores;
    private final IOwnersInteractor ownersInteractor;

    public UtilsInteractor(INetworker networker, IStores stores) {
        this.networker = networker;
        this.stores = stores;
        this.ownersInteractor = new OwnersInteractor(networker, stores.owners());
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig) {
        return Single.just(orig)
                .flatMap(map -> {
                    final Set<Integer> uids = new HashSet<>();
                    final Set<Integer> listsIds = new HashSet<>();

                    for (Map.Entry<?, SimplePrivacy> mapEntry : orig.entrySet()) {
                        SimplePrivacy privacy = mapEntry.getValue();

                        if (isNull(privacy) || isEmpty(privacy.getEntries())) {
                            continue;
                        }

                        for (SimplePrivacy.Entry entry : privacy.getEntries()) {
                            switch (entry.getType()) {
                                case SimplePrivacy.Entry.TYPE_FRIENDS_LIST:
                                    listsIds.add(entry.getId());
                                    break;
                                case SimplePrivacy.Entry.TYPE_USER:
                                    uids.add(entry.getId());
                                    break;
                            }
                        }
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, uids, IOwnersInteractor.MODE_ANY)
                            .flatMap(owners -> findFriendListsByIds(accountId, accountId, listsIds)
                                    .map(lists -> {
                                        Map<Integer, Privacy> privacies = new HashMap<>(Utils.safeCountOf(orig));

                                        for (Map.Entry<Integer, SimplePrivacy> entry : orig.entrySet()) {
                                            Integer key = entry.getKey();
                                            SimplePrivacy value = entry.getValue();

                                            Privacy full = Objects.isNull(value) ? null : Dto2Model.transform(value, owners, lists);
                                            privacies.put(key, full);
                                        }

                                        return privacies;
                                    }));
                });
    }

    @Override
    public Single<Optional<Owner>> resolveDomain(int accountId, String domain) {
        return stores.owners()
                .findUserByDomain(accountId, domain)
                .<Optional<Owner>>flatMap(optionalUserEntity -> {
                    if(optionalUserEntity.nonEmpty()){
                        User user = Entity2Model.buildUserFromDbo(optionalUserEntity.get());
                        return Single.just(Optional.wrap(user));
                    }

                    return stores.owners()
                            .findCommunityByDomain(accountId, domain)
                            .flatMap(optionalCommunityEntity -> {
                                if(optionalCommunityEntity.nonEmpty()){
                                    Community community = Entity2Model.buildCommunityFromDbo(optionalCommunityEntity.get());
                                    return Single.just(Optional.<Owner>wrap(community));
                                }

                                return Single.just(Optional.empty());
                            });
                })
                .flatMap(optionalOwner -> {
                    if(optionalOwner.nonEmpty()){
                        return Single.just(optionalOwner);
                    }

                    return networker.vkDefault(accountId)
                            .utils()
                            .resolveScreenName(domain)
                            .flatMap(response -> {
                                if("user".equals(response.type)){
                                    int userId = Integer.parseInt(response.object_id);
                                    return ownersInteractor.getBaseOwnerInfo(accountId, userId, IOwnersInteractor.MODE_ANY)
                                            .map(Optional::wrap);
                                }

                                if("group".equals(response.type)){
                                    int ownerId = -Math.abs(Integer.parseInt(response.object_id));
                                    return ownersInteractor.getBaseOwnerInfo(accountId, ownerId, IOwnersInteractor.MODE_ANY)
                                            .map(Optional::wrap);
                                }

                                return Single.just(Optional.empty());
                            });
                });
    }

    @SuppressLint("UseSparseArrays")
    private Single<Map<Integer, FriendList>> findFriendListsByIds(int accountId, int userId, @NonNull Collection<Integer> ids) throws Exception {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyMap());
        }

        return stores.owners()
                .findFriendsListsByIds(accountId, userId, ids)
                .flatMap(map -> {
                    if (map.size() == ids.size()) {
                        Map<Integer, FriendList> data = new HashMap<>(map.size());

                        for (int id : ids) {
                            FriendListEntity dbo = map.get(id);
                            data.put(id, new FriendList(dbo.getId(), dbo.getName()));
                        }

                        return Single.just(data);
                    }

                    return networker.vkDefault(accountId)
                            .friends()
                            .getLists(userId, true)
                            .map(items -> listEmptyIfNull(items.getItems()))
                            .flatMap(dtos -> {
                                List<FriendListEntity> dbos = new ArrayList<>(dtos.size());

                                final Map<Integer, FriendList> data = new HashMap<>(map.size());

                                for (VkApiFriendList dto : dtos) {
                                    dbos.add(new FriendListEntity(dto.id, dto.name));
                                }

                                for (int id : ids) {
                                    boolean found = false;

                                    for (VkApiFriendList dto : dtos) {
                                        if (dto.id == id) {
                                            data.put(id, Dto2Model.transform(dto));
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        map.put(id, new FriendListEntity(id, "UNKNOWN"));
                                    }
                                }

                                return stores.relativeship()
                                        .storeFriendsList(accountId, userId, dbos)
                                        .andThen(Single.just(data));
                            });
                });
    }
}
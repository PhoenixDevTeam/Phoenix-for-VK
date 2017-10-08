package biz.dealnote.messenger.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.interfaces.IWallStore;
import biz.dealnote.messenger.db.model.PostPatch;
import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.domain.mappers.Model2Dto;
import biz.dealnote.messenger.domain.mappers.Model2Entity;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.criteria.WallCriteria;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.subjects.PublishSubject;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public class WallsImpl implements IWalls {

    private final INetworker networker;

    private final IStores repositories;

    private final IOwnersInteractor ownersInteractor;

    private final PublishSubject<PostUpdate> minorUpdatesPublisher;

    private final PublishSubject<Post> majorUpdatesPublisher;

    private final PublishSubject<biz.dealnote.messenger.model.IdPair> postInvalidatePublisher;

    public WallsImpl(INetworker networker, IStores repositories) {
        this.minorUpdatesPublisher = PublishSubject.create();
        this.majorUpdatesPublisher = PublishSubject.create();
        this.postInvalidatePublisher = PublishSubject.create();

        this.networker = networker;
        this.repositories = repositories;
        this.ownersInteractor = new OwnersInteractor(networker, repositories.owners());
    }

    @Override
    public Completable editPost(int accountId, int ownerId, int postId, Boolean friendsOnly,
                                String message, List<AbsModel> attachments, String services,
                                Boolean signed, Long publishDate, Double latitude, Double longitude,
                                Integer placeId, Boolean markAsAds) {

        List<IAttachmentToken> tokens = null;

        try {
            if (nonEmpty(attachments)) {
                tokens = Model2Dto.createTokens(attachments);
            }
        } catch (Exception e) {
            return Completable.error(e);
        }

        return networker.vkDefault(accountId)
                .wall()
                .edit(ownerId, postId, friendsOnly, message, tokens, services,
                        signed, publishDate, latitude, longitude, placeId, markAsAds)
                .flatMapCompletable(aBoolean -> getAndStorePost(accountId, ownerId, postId).toCompletable());
    }

    @Override
    public Single<Post> post(int accountId, int ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                             List<AbsModel> attachments, String services, Boolean signed,
                             Long publishDate, Double latitude, Double longitude, Integer placeId,
                             final Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth) {

        List<IAttachmentToken> tokens = null;

        try {
            if (nonEmpty(attachments)) {
                tokens = Model2Dto.createTokens(attachments);
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        return networker.vkDefault(accountId)
                .wall()
                .post(ownerId, friendsOnly, fromGroup, message, tokens, services, signed, publishDate,
                        latitude, longitude, placeId, postId, guid, markAsAds, adsPromotedStealth)
                .flatMap(vkid -> {
                    Completable completable;

                    if (nonNull(postId) && !postId.equals(vkid)) {
                        // если id поста изменился - удаляем его из бд
                        completable = invalidatePost(accountId, postId, ownerId);
                    } else {
                        completable = Completable.complete();
                    }

                    return completable.andThen(getAndStorePost(accountId, ownerId, vkid));
                });
    }

    private Completable invalidatePost(int accountId, int postId, int ownerId) {
        biz.dealnote.messenger.model.IdPair pair = new biz.dealnote.messenger.model.IdPair(postId, ownerId);

        return repositories.wall()
                .invalidatePost(accountId, postId, ownerId)
                .doOnComplete(() -> postInvalidatePublisher.onNext(pair));
    }

    @Override
    public Single<Integer> like(int accountId, int ownerId, int postId, boolean add) {
        Single<Integer> single;
        if (add) {
            single = networker.vkDefault(accountId)
                    .likes()
                    .add("post", ownerId, postId, null);
        } else {
            single = networker.vkDefault(accountId)
                    .likes()
                    .delete("post", ownerId, postId);
        }

        return single.flatMap(count -> {
            // TODO: 05.09.2017 Сохранение лайков в таблице новостей надо ?
            final PostUpdate update = new PostUpdate(accountId, postId, ownerId).withLikes(count, add);
            return applyPatch(update).andThen(Single.just(count));
        });
    }

    private static PostPatch update2patch(PostUpdate data) {
        PostPatch patch = new PostPatch();
        if (nonNull(data.getDeleteUpdate())) {
            patch.withDeletion(data.getDeleteUpdate().isDeleted());
        }

        if (nonNull(data.getLikeUpdate())) {
            patch.withLikes(data.getLikeUpdate().getCount(), data.getLikeUpdate().isLiked());
        }

        if (nonNull(data.getPinUpdate())) {
            patch.withPin(data.getPinUpdate().isPinned());
        }

        return patch;
    }

    @Override
    public Single<List<Post>> getWall(int accountId, int ownerId, int offset, int count, int wallFilter) {
        return networker.vkDefault(accountId)
                .wall()
                .get(ownerId, null, offset, count, convertToApiFilter(wallFilter), true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    List<VKApiPost> dtos = Utils.listEmptyIfNull(response.posts);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }

                    final OwnerEntities ownerEntities = Dto2Entity.buildOwnerDbos(response.profiles, response.groups);
                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .flatMap(bundle -> {
                                List<Post> posts = Dto2Model.transformPosts(dtos, bundle);

                                List<PostEntity> dbos = new ArrayList<>(dtos.size());
                                for (VKApiPost dto : dtos) {
                                    dbos.add(Dto2Entity.buildPostDbo(dto));
                                }

                                return repositories.wall()
                                        .storeWallDbos(accountId, dbos, ownerEntities, offset == 0 ? () -> ownerId : null)
                                        .map(optional -> posts);
                            });
                });
    }

    private SingleTransformer<List<PostEntity>, List<Post>> dbos2models(int accountId) {
        return single -> single
                .flatMap(dbos -> {
                    final VKOwnIds ids = new VKOwnIds();
                    Entity2Model.fillOwnerIds(ids, dbos);

                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY)
                            .map(owners -> {
                                List<Post> posts = new ArrayList<>(dbos.size());
                                for (PostEntity dbo : dbos) {
                                    posts.add(Entity2Model.buildPostFromDbo(dbo, owners));
                                }
                                return posts;
                            });
                });
    }

    private SingleTransformer<PostEntity, Post> dbo2model(int accountId) {
        return single -> single
                .flatMap(dbo -> {
                    final VKOwnIds ids = new VKOwnIds();
                    Entity2Model.fillPostOwnerIds(ids, dbo);

                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY)
                            .map(owners -> {
                                return Entity2Model.buildPostFromDbo(dbo, owners);
                            });
                });
    }

    @Override
    public Single<List<Post>> getCachedWall(int accountId, int ownerId, int wallFilter) {
        WallCriteria criteria = new WallCriteria(accountId, ownerId).setMode(wallFilter);
        return repositories.wall()
                .findDbosByCriteria(criteria)
                .compose(dbos2models(accountId));
    }

    private Completable applyPatch(final PostUpdate update) {
        final PostPatch patch = update2patch(update);

        return repositories.wall()
                .update(update.getAccountId(), update.getOwnerId(), update.getPostId(), patch)
                .andThen(Completable.fromAction(() -> minorUpdatesPublisher.onNext(update)));
    }

    @Override
    public Completable delete(int accountId, int ownerId, int postId) {
        final PostUpdate update = new PostUpdate(accountId, postId, ownerId).withDeletion(true);
        return networker.vkDefault(accountId)
                .wall()
                .delete(ownerId, postId)
                .flatMapCompletable(igrored -> applyPatch(update));
    }

    @Override
    public Completable restore(int accountId, int ownerId, int postId) {
        final PostUpdate update = new PostUpdate(accountId, postId, ownerId).withDeletion(false);
        return networker.vkDefault(accountId)
                .wall()
                .restore(ownerId, postId)
                .flatMapCompletable(igrored -> applyPatch(update));
    }

    @Override
    public Single<Post> getById(int accountId, int ownerId, int postId) {
        final IdPair id = new IdPair(postId, ownerId);

        return networker.vkDefault(accountId)
                .wall()
                .getById(Collections.singleton(id), true, 5, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if(isEmpty(response.posts)){
                        throw new NotFoundException();
                    }

                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    List<VKApiPost> dtos = response.posts;
                    VKApiPost dto = dtos.get(0);

                    VKOwnIds ids = new VKOwnIds().append(dto);
                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .map(bundle -> Dto2Model.transform(dto, bundle));
                });
    }

    @Override
    public Completable pinUnpin(int accountId, int ownerId, int postId, boolean pin) {
        Single<Boolean> single;

        if (pin) {
            single = networker.vkDefault(accountId)
                    .wall()
                    .pin(ownerId, postId);
        } else {
            single = networker.vkDefault(accountId)
                    .wall()
                    .unpin(ownerId, postId);
        }

        PostUpdate update = new PostUpdate(accountId, postId, ownerId).withPin(pin);
        return single.flatMapCompletable(ignored -> applyPatch(update));
    }

    @Override
    public Observable<PostUpdate> observeMinorChanges() {
        return minorUpdatesPublisher;
    }

    @Override
    public Observable<Post> observeChanges() {
        return majorUpdatesPublisher;
    }

    @Override
    public Observable<biz.dealnote.messenger.model.IdPair> observePostInvalidation() {
        return postInvalidatePublisher;
    }

    @Override
    public Single<Post> getEditingPost(int accountId, int ownerId, int type, boolean withAttachments) {
        return repositories.wall()
                .getEditingPost(accountId, ownerId, type, withAttachments)
                .compose(dbo2model(accountId));
    }

    @Override
    public Single<Post> post(int accountId, Post post, boolean fromGroup, boolean showSigner) {
        final Long publishDate = post.isPostponed() ? post.getDate() : null;

        final List<AbsModel> attachments = post.hasAttachments() ? post.getAttachments().toList() : null;

        final Integer postponedPostId = post.isPostponed() ? (post.getVkid() > 0 ? post.getVkid() : null) : null;

        return post(accountId, post.getOwnerId(), post.isFriendsOnly(), fromGroup, post.getText(),
                attachments, null, showSigner, publishDate, null, null, null,
                postponedPostId, post.getDbid(), null, null);
    }

    @Override
    public Single<Post> repost(int accountId, int postId, int ownerId, Integer groupId, String message) {
        final int resultOwnerId = nonNull(groupId) ? -Math.abs(groupId) : accountId;
        return networker.vkDefault(accountId)
                .wall()
                .repost(ownerId, postId, message, groupId, null)
                .flatMap(reponse -> getAndStorePost(accountId, resultOwnerId, reponse.postId));
    }

    @Override
    public Single<Integer> cachePostWithIdSaving(int accountId, Post post) {
        final PostEntity dbo = Model2Entity.buildPostDbo(post);

        return repositories.wall()
                .replacePost(accountId, dbo);
    }

    @Override
    public Completable deleteFromCache(int accountId, int postDbid) {
        return repositories.wall()
                .deletePost(accountId, postDbid);
    }

    private static String convertToApiFilter(int filter) {
        switch (filter) {
            case WallCriteria.MODE_ALL:
                return "all";
            case WallCriteria.MODE_OWNER:
                return "owner";
            case WallCriteria.MODE_SCHEDULED:
                return "postponed";
            case WallCriteria.MODE_SUGGEST:
                return "suggests";
            default:
                throw new IllegalArgumentException("Invalid wall filter");
        }
    }

    private Single<Post> getAndStorePost(int accountId, int ownerId, int postId) {
        IWallStore cache = repositories.wall();

        return networker.vkDefault(accountId)
                .wall()
                .getById(singlePair(postId, ownerId), true, 5, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (safeCountOf(response.posts) != 1) {
                        throw new NotFoundException();
                    }

                    PostEntity dbo = Dto2Entity.buildPostDbo(response.posts.get(0));

                    OwnerEntities ownerEntities = Dto2Entity.buildOwnerDbos(response.profiles, response.groups);
                    return cache.storeWallDbos(accountId, Collections.singletonList(dbo), ownerEntities, null)
                            .map(ints -> ints[0])
                            .flatMap(dbid -> cache
                                    .findPostById(accountId, dbid)
                                    .map(Optional::get)
                                    .compose(dbo2model(accountId)));
                })
                .map(post -> {
                    majorUpdatesPublisher.onNext(post);
                    return post;
                });
    }

    private static Collection<IdPair> singlePair(int postId, int postOwnerId) {
        return Collections.singletonList(new IdPair(postId, postOwnerId));
    }

    @Override
    public Single<Pair<List<Post>, Integer>> search(int accountId, int ownerId, String query, boolean ownersPostOnly, int count, int offset) {
        return networker.vkDefault(accountId)
                .wall()
                .search(ownerId, query, ownersPostOnly, count, offset, true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiPost> dtos = Utils.listEmptyIfNull(response.items);
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .map(ownersBundle -> Pair.create(Dto2Model.transformPosts(dtos, ownersBundle), response.count));
                });
    }
}
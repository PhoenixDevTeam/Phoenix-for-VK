package biz.dealnote.messenger.domain.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.IAccountApis;
import biz.dealnote.messenger.api.interfaces.IBoardApi;
import biz.dealnote.messenger.api.interfaces.ILikesApi;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.interfaces.IPhotosApi;
import biz.dealnote.messenger.api.interfaces.IVideoApi;
import biz.dealnote.messenger.api.interfaces.IWallApi;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.response.DefaultCommentsResponse;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.domain.ICommentsInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Dto;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.domain.mappers.Model2Dto;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.CommentIntent;
import biz.dealnote.messenger.model.CommentUpdate;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.CommentedType;
import biz.dealnote.messenger.model.CommentsBundle;
import biz.dealnote.messenger.model.DraftComment;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.criteria.CommentsCriteria;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.BooleanSupplier;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;
import static java.util.Collections.emptyList;

/**
 * Created by Ruslan Kolbasa on 07.06.2017.
 * phoenix
 */
public class CommentsInteractor implements ICommentsInteractor {

    private final INetworker networker;

    private final IStores cache;

    private final IOwnersInteractor ownersInteractor;

    public CommentsInteractor(INetworker networker, IStores cache) {
        this.networker = networker;
        this.cache = cache;
        this.ownersInteractor = new OwnersInteractor(networker, cache.owners());
    }

    @Override
    public Single<List<Comment>> getAllCachedData(int accounrId, @NonNull Commented commented) {
        final CommentsCriteria criteria = new CommentsCriteria(accounrId, commented);
        return cache.comments()
                .getDbosByCriteria(criteria)
                .compose(dbos2models(accounrId));
    }

    private SingleTransformer<List<CommentEntity>, List<Comment>> dbos2models(int accountId) {
        return single -> single.flatMap(dbos -> {
            VKOwnIds ownids = new VKOwnIds();
            for (CommentEntity c : dbos) {
                Entity2Model.fillCommentOwnerIds(ownids, c);
            }

            return ownersInteractor
                    .findBaseOwnersDataAsBundle(accountId, ownids.getAll(), IOwnersInteractor.MODE_ANY)
                    .map(owners -> {
                        List<Comment> comments = new ArrayList<>(dbos.size());
                        for (CommentEntity dbo : dbos) {
                            comments.add(Entity2Model.buildCommentFromDbo(dbo, owners));
                        }
                        return comments;
                    });
        });
    }

    private Completable cacheData(int accountId, @NonNull Commented commented, List<CommentEntity> data, OwnerEntities owners, boolean invalidateCache) {
        final int sourceId = commented.getSourceId();
        final int ownerId = commented.getSourceOwnerId();
        final int type = commented.getSourceType();

        return Single.just(data)
                .flatMapCompletable(dbos -> cache.comments().insert(accountId, sourceId, ownerId, type, dbos, owners, invalidateCache)
                        .toCompletable());
    }

    private Single<List<Comment>> transform(int accountId, @NonNull Commented commented, @NonNull List<VKApiComment> comments, Collection<VKApiUser> users, Collection<VKApiCommunity> groups) {
        VKOwnIds ownids = new VKOwnIds();
        for (VKApiComment dto : comments) {
            ownids.append(dto);
        }

        return ownersInteractor
                .findBaseOwnersDataAsBundle(accountId, ownids.getAll(), IOwnersInteractor.MODE_ANY, Dto2Model.transformOwners(users, groups))
                .map(bundle -> {
                    List<Comment> data = new ArrayList<>(comments.size());
                    for (VKApiComment dto : comments) {
                        data.add(Dto2Model.buildComment(commented, dto, bundle));
                    }

                    Collections.sort(data, (o1, o2) -> (o2.getId() < o1.getId()) ? -1 : ((o2.getId() == o1.getId()) ? 0 : 1));
                    return data;
                });
    }

    @Override
    public Single<CommentsBundle> getCommentsPortion(int accountId, @NonNull Commented commented, int offset, int count, Integer startCommentId, boolean invalidateCache, String sort) {
        final String type = commented.getTypeForStoredProcedure();

        return networker.vkDefault(accountId)
                .comments()
                .get(type, commented.getSourceOwnerId(), commented.getSourceId(), offset, count, sort, startCommentId, commented.getAccessKey(), Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiComment> commentDtos = nonNull(response.main) ? listEmptyIfNull(response.main.comments) : emptyList();
                    List<VKApiUser> users = nonNull(response.main) ? listEmptyIfNull(response.main.profiles) : emptyList();
                    List<VKApiCommunity> groups = nonNull(response.main) ? listEmptyIfNull(response.main.groups) : emptyList();

                    Single<List<Comment>> modelsSingle = transform(accountId, commented, commentDtos, users, groups);

                    List<CommentEntity> dbos = new ArrayList<>(commentDtos.size());
                    for (VKApiComment dto : commentDtos) {
                        dbos.add(Dto2Entity.buildCommentDbo(commented.getSourceId(), commented.getSourceOwnerId(), commented.getSourceType(), commented.getAccessKey(), dto));
                    }

                    return cacheData(accountId, commented, dbos, Dto2Entity.buildOwnerDbos(users, groups), invalidateCache)
                            .andThen(modelsSingle.map(data -> {
                                CommentsBundle bundle = new CommentsBundle(data)
                                        .setAdminLevel(response.admin_level)
                                        .setFirstCommentId(response.firstId)
                                        .setLastCommentId(response.lastId);

                                if (nonNull(response.main) && nonNull(response.main.poll)) {
                                    Poll poll = Dto2Model.transform(response.main.poll);
                                    poll.setBoard(true); // так как это может быть только из топика
                                    bundle.setTopicPoll(poll);
                                }

                                return bundle;
                            }));
                });
    }

    @Override
    public Maybe<DraftComment> restoreDraftComment(int accountId, @NonNull Commented commented) {
        return cache.comments()
                .findEditingComment(accountId, commented);
    }

    @Override
    public Single<Integer> safeDraftComment(int accountId, @NonNull Commented commented, String body, int replyToCommentId, int replyToUserId) {
        return cache.comments()
                .saveDraftComment(accountId, commented, body, replyToUserId, replyToCommentId);
    }

    @Override
    public Completable like(int accountId, Commented commented, int commentId, boolean add) {
        final String type;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                type = "photo_comment";
                break;
            case CommentedType.POST:
                type = "comment";
                break;
            case CommentedType.VIDEO:
                type = "video_comment";
                break;
            case CommentedType.TOPIC:
                type = "topic_comment";
                break;

            default:
                throw new IllegalArgumentException();
        }

        final ILikesApi api = networker.vkDefault(accountId).likes();

        final CommentUpdate update = CommentUpdate.create(accountId, commented, commentId);

        if (add) {
            return api.add(type, commented.getSourceOwnerId(), commentId, commented.getAccessKey())
                    .flatMapCompletable(count -> {
                        update.withLikes(true, count);
                        return cache.comments().commitMinorUpdate(update);
                    });
        } else {
            return api.delete(type, commented.getSourceOwnerId(), commentId)
                    .flatMapCompletable(count -> {
                        update.withLikes(false, count);
                        return cache.comments().commitMinorUpdate(update);
                    });
        }
    }

    @Override
    public Completable deleteRestore(int accountId, Commented commented, int commentId, boolean delete) {
        final IAccountApis apis = networker.vkDefault(accountId);
        final int ownerId = commented.getSourceOwnerId();
        final CommentUpdate update = CommentUpdate.create(accountId, commented, commentId)
                .withDeletion(delete);

        Single<Boolean> single;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                IPhotosApi photosApi = apis.photos();
                if (delete) {
                    single = photosApi.deleteComment(ownerId, commentId);
                } else {
                    single = photosApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.POST:
                IWallApi wallApi = apis.wall();
                if (delete) {
                    single = wallApi.deleteComment(ownerId, commentId);
                } else {
                    single = wallApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.VIDEO:
                IVideoApi videoApi = apis.video();
                if (delete) {
                    single = videoApi.deleteComment(ownerId, commentId);
                } else {
                    single = videoApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.TOPIC:
                int groupId = Math.abs(ownerId);
                int topicId = commented.getSourceId();

                IBoardApi boardApi = apis.board();
                if (delete) {
                    single = boardApi.deleteComment(groupId, topicId, commentId);
                } else {
                    single = boardApi.restoreComment(groupId, topicId, commentId);
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return single.flatMapCompletable(ignore -> cache
                .comments()
                .commitMinorUpdate(update));
    }

    @Override
    public Single<Comment> send(int accountId, Commented commented, final CommentIntent intent) {
        final Single<List<IAttachmentToken>> cachedAttachments;

        if (nonNull(intent.getDraftMessageId())) {
            cachedAttachments = getCachedAttachmentsToken(accountId, intent.getDraftMessageId());
        } else {
            cachedAttachments = Single.just(emptyList());
        }

        return cachedAttachments
                .flatMap(cachedTokens -> {
                    final List<IAttachmentToken> tokens = new ArrayList<>();

                    if (nonNull(cachedTokens)) {
                        tokens.addAll(cachedTokens);
                    }

                    if (nonEmpty(intent.getModels())) {
                        tokens.addAll(Model2Dto.createTokens(intent.getModels()));
                    }

                    return sendComment(accountId, commented, intent, tokens)
                            .flatMap(id -> getCommentByIdAndStore(accountId, commented, id, true))
                            .flatMap(comment -> {
                                if (isNull(intent.getDraftMessageId())) {
                                    return Single.just(comment);
                                }

                                return cache.comments()
                                        .deleteByDbid(accountId, intent.getDraftMessageId())
                                        .andThen(Single.just(comment));
                            });
                });
    }

    private Single<List<IAttachmentToken>> getCachedAttachmentsToken(int accountId, int commentDbid) {
        return cache.attachments()
                .getAttachmentsDbosWithIds(accountId, AttachToType.COMMENT, commentDbid)
                .map(pairs -> {
                    List<IAttachmentToken> tokens = new ArrayList<>(pairs.size());
                    for (Pair<Integer, Entity> pair : pairs) {
                        tokens.add(Entity2Dto.createToken(pair.getSecond()));
                    }

                    return tokens;
                });
    }

    @Override
    public Single<List<Comment>> getAllCommentsRange(int accountId, Commented commented, int startFromCommentId, int continueToCommentId) {
        final TempData tempData = new TempData();

        BooleanSupplier booleanSupplier = () -> {
            for (VKApiComment c : tempData.comments) {
                if (continueToCommentId == c.id) {
                    return true;
                }
            }

            return false;
        };

        Completable completable = startLooking(accountId, commented, tempData, startFromCommentId, continueToCommentId)
                .repeatUntil(booleanSupplier);

        return completable.toSingleDefault(tempData)
                .flatMap(data -> transform(accountId, commented, data.comments, data.profiles, data.groups));
    }

    @Override
    public Single<List<Owner>> getAvailableAuthors(int accountId) {
        return ownersInteractor.getBaseOwnerInfo(accountId, accountId, IOwnersInteractor.MODE_ANY)
                .flatMap(owner -> networker.vkDefault(accountId)
                        .groups()
                        .get(accountId, true, "admin,editor", GroupColumns.API_FIELDS, null, 1000)
                        .map(Items::getItems)
                        .map(groups -> {
                            List<Owner> owners = new ArrayList<>(groups.size() + 1);
                            owners.add(owner);
                            owners.addAll(Dto2Model.transformCommunities(groups));
                            return owners;
                        }));
    }

    @Override
    public Single<Comment> edit(int accountId, Commented commented, int commentId, String body, List<AbsModel> attachments) {
        List<IAttachmentToken> tokens = new ArrayList<>();

        try {
            if (nonNull(attachments)) {
                tokens.addAll(Model2Dto.createTokens(attachments));
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        Single<Boolean> editSingle;

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                editSingle = networker
                        .vkDefault(accountId)
                        .wall()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            case CommentedType.PHOTO:
                editSingle = networker
                        .vkDefault(accountId)
                        .photos()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            case CommentedType.TOPIC:
                int groupId = Math.abs(commented.getSourceOwnerId());
                int topicId = commented.getSourceId();

                editSingle = networker
                        .vkDefault(accountId)
                        .board()
                        .editComment(groupId, topicId, commentId, body, tokens);
                break;

            case CommentedType.VIDEO:
                editSingle = networker
                        .vkDefault(accountId)
                        .video()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            default:
                return Single.error(new IllegalArgumentException("Unknown commented source type"));
        }

        return editSingle.flatMap(ignored -> getCommentByIdAndStore(accountId, commented, commentId, true));
    }

    private Completable startLooking(int accountId, Commented commented, TempData tempData, int startFromCommentId, int continueToCommentId) {
        final int[] tryNumber = {0};

        return Single
                .fromCallable(() -> {
                    tryNumber[0]++;

                    if (tryNumber[0] == 1) {
                        return startFromCommentId;
                    }

                    if (tempData.comments.isEmpty()) {
                        throw new NotFoundException();
                    }

                    VKApiComment older = tempData.comments.get(tempData.comments.size() - 1);

                    if (older.id < continueToCommentId) {
                        throw new NotFoundException();
                    }

                    return older.id;
                }).flatMapCompletable(id -> getDefaultCommentsService(accountId, commented, id, 1, 100, "desc", true, Constants.MAIN_OWNER_FIELDS)
                        .map(response -> {
                                    tempData.append(response, continueToCommentId);
                                    return response;
                                }
                        ).toCompletable());
    }

    private Single<DefaultCommentsResponse> getDefaultCommentsService(int accountId, Commented commented, Integer startCommentId,
                                                                      Integer offset, Integer count, String sort, Boolean extended, String fields) {
        int ownerId = commented.getSourceOwnerId();
        int sourceId = commented.getSourceId();

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                return networker.vkDefault(accountId)
                        .wall()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, extended, fields);
            case CommentedType.PHOTO:
                return networker.vkDefault(accountId)
                        .photos()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, commented.getAccessKey(), extended, fields);
            case CommentedType.VIDEO:
                return networker.vkDefault(accountId)
                        .video()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, extended, fields);
            case CommentedType.TOPIC:
                return networker.vkDefault(accountId)
                        .board()
                        .getComments(Math.abs(ownerId), sourceId, true, startCommentId, offset, count, extended, sort, fields);
        }

        throw new UnsupportedOperationException();
    }

    private static final class TempData {

        Set<VKApiUser> profiles = new HashSet<>();
        Set<VKApiCommunity> groups = new HashSet<>();
        List<VKApiComment> comments = new ArrayList<>();

        void append(DefaultCommentsResponse response, int continueToCommentId) {
            if (nonNull(response.groups)) {
                groups.addAll(response.groups);
            }

            if (nonNull(response.profiles)) {
                profiles.addAll(response.profiles);
            }

            boolean hasTargetComment = false;
            int additionalCount = 0;

            for (VKApiComment comment : response.items) {
                if (comment.id == continueToCommentId) {
                    hasTargetComment = true;
                } else if (hasTargetComment) {
                    additionalCount++;
                }

                comments.add(comment);

                if (additionalCount > 5) {
                    break;
                }
            }
        }
    }

    private Single<Integer> sendComment(int accountId, @NonNull Commented commented, @NonNull CommentIntent intent, @Nullable List<IAttachmentToken> attachments) {
        IAccountApis apies = networker.vkDefault(accountId);

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                Integer fromGroup = intent.getAuthorId() < 0 ? Math.abs(intent.getAuthorId()) : null;

                return apies.wall()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                fromGroup, intent.getMessage(), intent.getReplyToComment(),
                                attachments, intent.getStickerId(), intent.getDraftMessageId());

            case CommentedType.PHOTO:
                return apies.photos()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                intent.getAuthorId() < 0, intent.getMessage(), intent.getReplyToComment(),
                                attachments, intent.getStickerId(), commented.getAccessKey(), intent.getDraftMessageId());

            case CommentedType.VIDEO:
                return apies.video()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                intent.getMessage(), attachments, intent.getAuthorId() < 0,
                                intent.getReplyToComment(), intent.getStickerId(), intent.getDraftMessageId());

            case CommentedType.TOPIC:
                int topicGroupId = Math.abs(commented.getSourceOwnerId());

                return apies.board()
                        .addComment(topicGroupId, commented.getSourceId(), intent.getMessage(),
                                attachments, intent.getAuthorId() < 0, intent.getStickerId(), intent.getDraftMessageId());
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Single<Comment> getCommentByIdAndStore(int accountId, Commented commented, int commentId, boolean storeToCache) {
        final String type = commented.getTypeForStoredProcedure();
        final int sourceId = commented.getSourceId();
        final int ownerId = commented.getSourceOwnerId();
        final int sourceType = commented.getSourceType();

        return networker.vkDefault(accountId)
                .comments()
                .get(type, commented.getSourceOwnerId(), commented.getSourceId(), 0, 1,
                        null, commentId, commented.getAccessKey(), Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (isNull(response.main) || safeCountOf(response.main.comments) != 1) {
                        throw new NotFoundException();
                    }

                    List<VKApiComment> comments = response.main.comments;
                    List<VKApiUser> users = response.main.profiles;
                    List<VKApiCommunity> communities = response.main.groups;

                    Completable storeCompletable;
                    if (storeToCache) {
                        List<CommentEntity> dbos = new ArrayList<>(comments.size());
                        for (VKApiComment dto : comments) {
                            dbos.add(Dto2Entity.buildCommentDbo(commented.getSourceId(), commented.getSourceOwnerId(), commented.getSourceType(), commented.getAccessKey(), dto));
                        }

                        storeCompletable = cache.comments()
                                .insert(accountId, sourceId, ownerId, sourceType, dbos, Dto2Entity.buildOwnerDbos(users, communities), false)
                                .toCompletable();
                    } else {
                        storeCompletable = Completable.complete();
                    }

                    return storeCompletable.andThen(transform(accountId, commented, comments, users, communities)
                            .map(data -> data.get(0)));
                });
    }
}
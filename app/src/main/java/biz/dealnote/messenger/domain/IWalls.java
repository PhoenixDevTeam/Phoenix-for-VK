package biz.dealnote.messenger.domain;

import android.support.annotation.CheckResult;

import java.util.List;

import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.IdPair;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public interface IWalls {

    @CheckResult
    Completable editPost(int accountId, int ownerId, int postId, Boolean friendsOnly, String message,
                         List<AbsModel> attachments, String services,
                         Boolean signed, Long publishDate, Double latitude,
                         Double longitude, Integer placeId, Boolean markAsAds);

    Single<Pair<List<Post>, Integer>> search(int accountId, int ownerId, String query, boolean ownersPostOnly, int count, int offset);

    Single<Post> post(int accountId, int ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                      List<AbsModel> attachments, String services, Boolean signed,
                      Long publishDate, Double latitude, Double longitude, Integer placeId,
                      Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth);

    Single<Integer> like(int accountId, int ownerId, int postId, boolean add);

    Single<List<Post>> getWall(int accountId, int ownerId, int offset, int count, int wallFilter);

    Single<List<Post>> getCachedWall(int accountId, int ownerId, int wallFilter);

    Completable delete(int accountId, int ownerId, int postId);

    Completable restore(int accountId, int ownerId, int postId);

    Single<Post> getById(int accountId, int ownerId, int postId);

    Completable pinUnpin(int accountId, int ownerId, int postId, boolean pin);

    /**
     * Ability to observe minor post changes (likes, deleted, pin state, etc.)
     */
    Observable<PostUpdate> observeMinorChanges();

    /**
     *
     */
    Observable<Post> observeChanges();

    /**
     * @return onNext в том случае, если пост перестал существовать
     */
    Observable<IdPair> observePostInvalidation();

    /**
     * Получить пост-черновик
     * @param accountId идентификатор аккаунта
     * @param ownerId идентификатор владельца стены
     * @param type тип (черновик или временный пост)
     * @param withAttachments если true - загрузить вложения поста
     * @return Single c обьектом поста
     */
    Single<Post> getEditingPost(int accountId, int ownerId, @EditingPostType int type, boolean withAttachments);

    Single<Post> post(int accountId, Post post, boolean fromGroup, boolean showSigner);

    Single<Post> repost(int accountId, int postId, int ownerId, Integer groupId, String message);

    /**
     * Сохранить пост в базу с тем же локальным идентификатором
     * @param accountId идентификатор аккаунта
     * @param post пост
     * @return Single с локальным идентификатором
     */
    Single<Integer> cachePostWithIdSaving(int accountId, Post post);

    /**
     * Удалить пост из кеша (используется только для "черновиков"
     * @param accountId идентификатор аккаунта
     * @param postDbid локальный идентификатор поста в БД
     * @return Completable
     */
    @CheckResult
    Completable deleteFromCache(int accountId, int postDbid);
}
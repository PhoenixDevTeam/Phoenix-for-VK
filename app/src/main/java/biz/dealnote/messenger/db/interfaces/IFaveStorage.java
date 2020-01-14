package biz.dealnote.messenger.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.FaveGroupEntity;
import biz.dealnote.messenger.db.model.entity.FaveLinkEntity;
import biz.dealnote.messenger.db.model.entity.FaveUserEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.criteria.FavePhotosCriteria;
import biz.dealnote.messenger.model.criteria.FavePostsCriteria;
import biz.dealnote.messenger.model.criteria.FaveVideosCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 28.05.2016.
 * VKMessenger
 */
public interface IFaveStorage extends IStorage {

    @CheckResult
    Single<List<PostEntity>> getFavePosts(@NonNull FavePostsCriteria criteria);

    @CheckResult
    Completable storePosts(int accountId, List<PostEntity> posts, OwnerEntities owners, boolean clearBeforeStore);

    @CheckResult
    Single<List<FaveLinkEntity>> getFaveLinks(int accountId);

    Completable removeLink(int accountId, String id);

    Completable storeLinks(int accountId, List<FaveLinkEntity> entities, boolean clearBefore);

    @CheckResult
    Completable storeUsers(int accountId, List<FaveUserEntity> users, boolean clearBeforeStore);

    @CheckResult
    Completable storeGroups(int accountId, List<FaveGroupEntity> groups, boolean clearBeforeStore);

    Single<List<FaveUserEntity>> getFaveUsers(int accountId);

    Single<List<FaveGroupEntity>> getFaveGroups(int accountId);

    Completable removeUser(int accountId, int userId);

    @CheckResult
    Single<int[]> storePhotos(int accountId, List<PhotoEntity> photos, boolean clearBeforeStore);

    @CheckResult
    Single<List<PhotoEntity>> getPhotos(FavePhotosCriteria criteria);

    @CheckResult
    Single<List<VideoEntity>> getVideos(FaveVideosCriteria criteria);

    @CheckResult
    Single<int[]> storeVideos(int accountId, List<VideoEntity> videos, boolean clearBeforeStore);
}
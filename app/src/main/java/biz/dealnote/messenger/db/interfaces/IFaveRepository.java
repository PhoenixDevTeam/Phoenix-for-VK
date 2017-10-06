package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.db.model.entity.UserEntity;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.model.criteria.FavePhotosCriteria;
import biz.dealnote.messenger.model.criteria.FavePostsCriteria;
import biz.dealnote.messenger.model.criteria.FaveVideosCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 28.05.2016.
 * VKMessenger
 */
public interface IFaveRepository extends IRepository {

    @CheckResult
    Single<List<PostEntity>> getFavePosts(@NonNull FavePostsCriteria criteria);

    @CheckResult
    Completable storePosts(int accountId, List<PostEntity> posts, OwnerEntities owners, boolean clearBeforeStore);

    @CheckResult
    Single<List<FaveLink>> getFaveLinks(int accountId);

    @CheckResult
    Completable storeUsers(int accountId, List<UserEntity> users, boolean clearBeforeStore);

    Single<List<UserEntity>> getFaveUsers(int accountId);

    @CheckResult
    Single<int[]> storePhotos(int accountId, List<PhotoEntity> photos, boolean clearBeforeStore);

    @CheckResult
    Single<List<PhotoEntity>> getPhotos(FavePhotosCriteria criteria);

    @CheckResult
    Single<List<VideoEntity>> getVideos(FaveVideosCriteria criteria);

    @CheckResult
    Single<int[]> storeVideos(int accountId, List<VideoEntity> videos, boolean clearBeforeStore);
}
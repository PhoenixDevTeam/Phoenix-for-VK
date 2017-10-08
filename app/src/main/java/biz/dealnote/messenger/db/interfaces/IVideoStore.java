package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.VideoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoStore extends IStore {

    @CheckResult
    Single<List<VideoEntity>> findByCriteria(@NonNull VideoCriteria criteria);

    @CheckResult
    Completable insertData(int accountId, int ownerId, int albumId, List<VideoEntity> videos, boolean invalidateBefore);
}
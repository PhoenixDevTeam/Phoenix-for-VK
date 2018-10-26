package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.VideoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoStorage extends IStorage {

    @CheckResult
    Single<List<VideoEntity>> findByCriteria(@NonNull VideoCriteria criteria);

    @CheckResult
    Completable insertData(int accountId, int ownerId, int albumId, List<VideoEntity> videos, boolean invalidateBefore);
}
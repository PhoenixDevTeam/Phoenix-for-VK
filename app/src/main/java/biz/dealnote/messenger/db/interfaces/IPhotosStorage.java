package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.db.model.PhotoPatch;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.model.criteria.PhotoCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IPhotosStorage extends IStorage {

    Completable insertPhotosRx(int accountId, int ownerId, int albumId, @NonNull List<PhotoEntity> photos, boolean clearBefore);

    Single<List<PhotoEntity>> findPhotosByCriteriaRx(@NonNull PhotoCriteria criteria);

    Completable applyPatch(int accountId, int ownerId, int photoId, PhotoPatch patch);
}
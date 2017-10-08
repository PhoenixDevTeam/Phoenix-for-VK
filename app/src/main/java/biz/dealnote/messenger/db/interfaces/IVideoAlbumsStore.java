package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.VideoAlbumEntity;
import biz.dealnote.messenger.model.VideoAlbumCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoAlbumsStore extends IStore {
    Single<List<VideoAlbumEntity>> findByCriteria(@NonNull VideoAlbumCriteria criteria);
    Completable insertData(int accountId, int ownerId, @NonNull List<VideoAlbumEntity> data, boolean invalidateBefore);
}
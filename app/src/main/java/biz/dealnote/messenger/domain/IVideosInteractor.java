package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.fragment.search.criteria.VideoSearchCriteria;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VideoAlbum;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public interface IVideosInteractor {
    Single<List<Video>> get(int accountId, int ownerId, int albumId, int count, int offset);
    Single<List<Video>> getCachedVideos(int accountId, int ownerId, int albumId);

    Single<Video> getById(int accountId, int ownerId, int videoId, String accessKey, boolean cache);
    Completable addToMy(int accountId, int targetOwnerId, int videoOwnerId, int videoId);

    Single<Pair<Integer, Boolean>> likeOrDislike(int accountId, int ownerId, int videoId, String accessKey, boolean like);

    Single<List<VideoAlbum>> getCachedAlbums(int accoutnId, int ownerId);
    Single<List<VideoAlbum>> getActualAlbums(int accoutnId, int ownerId, int count, int offset);

    Single<List<Video>> seacrh(int accountId, VideoSearchCriteria criteria, int count, int offset);
}
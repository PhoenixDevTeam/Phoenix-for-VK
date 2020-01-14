package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.EndlessData;
import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.model.FavePage;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Video;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public interface IFaveInteractor {
    Single<List<Post>> getPosts(int accountId, int count, int offset);
    Single<List<Post>> getCachedPosts(int accountId);

    Single<List<Photo>> getCachedPhotos(int accountId);
    Single<List<Photo>> getPhotos(int accountId, int count, int offset);

    Single<List<Video>> getCachedVideos(int accountId);
    Single<List<Video>> getVideos(int accountId, int count, int offset);

    Single<List<FavePage>> getCachedUsers(int accountId);

    Single<EndlessData<FavePage>> getUsers(int accountId, int count, int offset);

    Single<List<FavePage>> getCachedGroups(int accountId);

    Single<EndlessData<FavePage>> getGroups(int accountId, int count, int offset);

    Completable removeUser(int accountId, int userId);

    Single<List<FaveLink>> getCachedLinks(int accountId);
    Single<EndlessData<FaveLink>> getLinks(int accountId, int count, int offset);
    Completable removeLink(int accountId, String id);

    Completable addUser(int accountId, int userId);
}
package biz.dealnote.messenger.interactor;

import java.util.List;

import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.User;
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

    Single<List<User>> getCachedUsers(int accountId);
    Single<List<User>> getUsers(int accountId, int count, int offset);

    Completable addUser(int accountId, int userId);
}
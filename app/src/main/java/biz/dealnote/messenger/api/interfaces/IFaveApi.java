package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.FavePostsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFaveApi {

    @CheckResult
    Single<Items<VKApiUser>> getUsers(Integer offset, Integer count, String fields);

    @CheckResult
    Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, Boolean extended);

    @CheckResult
    Single<FavePostsResponse> getPosts(Integer offset, Integer count, Boolean extended);

    @CheckResult
    Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count);

    @CheckResult
    Single<Boolean> addGroup(int groupId);

    @CheckResult
    Single<Boolean> addUser(int userId);

    @CheckResult
    Single<Boolean> removeUser(int userId);

    @CheckResult
    Single<Boolean> removeLink(String linkId);

}

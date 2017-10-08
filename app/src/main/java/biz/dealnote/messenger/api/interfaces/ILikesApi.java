package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.LikesListResponse;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface ILikesApi {

    @CheckResult
    Single<LikesListResponse> getList(String type, Integer ownerId, Integer itemId, String pageUrl, String filter,
                                      Boolean friendsOnly, Integer offset, Integer count, Boolean skipOwn, String fields);

    @CheckResult
    Single<Integer> delete(String type, Integer ownerId, int itemId);

    @CheckResult
    Single<Integer> add(String type, Integer ownerId, int itemId, String accessKey);

}

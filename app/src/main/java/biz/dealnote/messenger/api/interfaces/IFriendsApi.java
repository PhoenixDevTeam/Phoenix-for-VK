package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import java.util.List;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VkApiFriendList;
import biz.dealnote.messenger.api.model.response.DeleteFriendResponse;
import biz.dealnote.messenger.api.model.response.OnlineFriendsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 30.12.2016.
 * phoenix
 */
public interface IFriendsApi {

    @CheckResult
    Single<OnlineFriendsResponse> getOnline(int userId, String order, int count,
                                            int offset, String fields);
    @CheckResult
    Single<Items<VKApiUser>> get(Integer userId, String order, Integer listId, Integer count, Integer offset,
                                 String fields, String nameCase);

    @CheckResult
    Single<Items<VkApiFriendList>> getLists(Integer userId, Boolean returnSystem);

    @CheckResult
    Single<DeleteFriendResponse> delete(int userId);

    @CheckResult
    Single<Integer> add(int userId, String text, Boolean follow);

    @CheckResult
    Single<Items<VKApiUser>> search(int userId, String query, String fields, String nameCase,
                                    Integer offset, Integer count);

    @CheckResult
    Single<List<VKApiUser>> getMutual(Integer sourceUid, int targetUid, int count, int offset, String fields);

}

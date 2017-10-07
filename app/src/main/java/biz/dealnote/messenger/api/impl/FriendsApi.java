package biz.dealnote.messenger.api.impl;

import java.util.List;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.interfaces.IFriendsApi;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VkApiFriendList;
import biz.dealnote.messenger.api.model.response.DeleteFriendResponse;
import biz.dealnote.messenger.api.model.response.OnlineFriendsResponse;
import biz.dealnote.messenger.api.services.IFriendsService;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.Single;

/**
 * Created by admin on 30.12.2016.
 * phoenix
 */
class FriendsApi extends AbsApi implements IFriendsApi {

    FriendsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<OnlineFriendsResponse> getOnline(int userId, String order, int count, int offset, String fields) {
        String targetOrder = Objects.isNull(order) ? null : toQuotes(order);
        String targetFields = Objects.isNull(fields) ? null : toQuotes(fields);

        String code = "var user_id = %s;\n" +
                "var count = %s;\n" +
                "var offset = %s;\n" +
                "var fields = %s;\n" +
                "\n" +
                "var uids = API.friends.getOnline({\n" +
                "    \"user_id\":user_id, \n" +
                "    \"count\":count, \n" +
                "    \"offset\":offset,\n" +
                "    \"order\":%s\n" +
                "});\n" +
                "\n" +
                "var profiles = API.users.get({\"user_ids\":uids, \"fields\":fields});\n" +
                "\n" +
                "return {\"uids\":uids, \"profiles\":profiles};";

        String formattedCode = String.format(code, userId, count, offset, targetFields, targetOrder);
        return provideService(IFriendsService.class)
                .flatMap(service -> service
                        .getOnline(formattedCode)
                        .map(extractResponseWithErrorHandling()));
    }

    /*@Override
    public Single<FriendsWithCountersResponse> getWithCounters(int userId, String order, int count,
                                                               int offset, String fields) {
        String targetOrder = Objects.isNull(order) ? null : toQuotes(order);
        String targetFields = Objects.isNull(fields) ? null : toQuotes(fields);

        String code = "var friends = API.friends.get({" +
                "\"user_id\":" + userId + ", " +
                "\"fields\":" + targetFields + ", " +
                "\"order\":" + targetOrder + ", " +
                "\"count\":" + count + ", " +
                "\"offset\":" + offset + "}); " +

                "var counters = API.users.get({\"user_ids\":" + userId + ", \"fields\":\"counters\"})[0].counters; " +

                "return {\"friends\":friends, \"counters\":counters};";

        return provideService(IFriendsService.class)
                .flatMap(service -> service
                        .getWithMyCounters(code)
                        .map(extractResponseWithErrorHandling()));
    }*/

    @Override
    public Single<Items<VKApiUser>> get(Integer userId, String order, Integer listId, Integer count,
                                        Integer offset, String fields, String nameCase) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.get(userId, order, listId, count, offset, fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiFriendList>> getLists(Integer userId, Boolean returnSystem) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.getLists(userId, integerFromBoolean(returnSystem))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DeleteFriendResponse> delete(int userId) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.delete(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> add(int userId, String text, Boolean follow) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.add(userId, text, integerFromBoolean(follow))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> search(int userId, String query, String fields, String nameCase, Integer offset, Integer count) {
        return provideService(IFriendsService.class)
                .flatMap(service -> service.search(userId, query, fields, nameCase, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiUser>> getMutual(Integer sourceUid, int targetUid, int count, int offset, String fields) {
        String code = "var source_uid = %s;\n" +
                "var target_uid = %s;\n" +
                "var count = %s;\n" +
                "var offset = %s;\n" +
                "var fields = %s;\n" +
                "\n" +
                "var uids = API.friends.getMutual({\n" +
                "    \"source_uid\":source_uid, \n" +
                "    \"target_uid\":target_uid, \n" +
                "    \"count\":count, \n" +
                "    \"offset\":offset\n" +
                "});\n" +
                "\n" +
                "var profiles = API.users.get({\"user_ids\":uids, \"fields\":fields});\n" +
                "\n" +
                "return {\"uids\":uids, \"profiles\":profiles};";

        String formattedCode = String.format(code, sourceUid, targetUid, count, offset, toQuotes(fields));

        //return executionService()
        //        .execute(formattedCode)
        //        .map(response -> {
        //            MutualFriendsResponse data = convertJsonResponse(response.get(), MutualFriendsResponse.class);
        //            return data.profiles;
        //        });

        return provideService(IFriendsService.class)
                .flatMap(service -> service.getMutual(formattedCode)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.profiles));
    }
}

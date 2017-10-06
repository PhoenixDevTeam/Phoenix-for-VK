package biz.dealnote.messenger.service.operations.feed;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.List;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.db.Repositories;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class FeedGetListOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        List<VkApiFeedList> lists = Apis.get()
                .vkDefault(accountId)
                .newsfeed()
                .getLists(null)
                .blockingGet()
                .getItems();

        Repositories.getInstance()
                .feed()
                .storeLists(accountId, lists)
                .blockingAwait();
        return null;
    }
}

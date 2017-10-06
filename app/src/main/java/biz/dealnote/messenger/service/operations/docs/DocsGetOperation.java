package biz.dealnote.messenger.service.operations.docs;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.db.Repositories;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DocsGetOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {

        int ownerId = request.getInt(Extra.OWNER_ID);

        //BaseResponse<Items<VkApiDoc>> response = RetrofitFactory.docs(accountId)
        //        .get(ownerId, null, null, null)
        //        .blockingGet();

        List<VkApiDoc> dtos = Apis.get()
                .vkDefault(accountId)
                .docs()
                .get(ownerId, null, null, null)
                .blockingGet()
                .getItems();

        Repositories.getInstance()
                .docs()
                .store(accountId, ownerId, dtos, true)
                .blockingGet();

        return null;
    }
}

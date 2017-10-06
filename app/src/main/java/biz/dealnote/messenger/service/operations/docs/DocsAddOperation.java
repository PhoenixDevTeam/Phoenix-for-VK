package biz.dealnote.messenger.service.operations.docs;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DocsAddOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);
        int ownerId = request.getInt(Extra.OWNER_ID);
        String accessKey = request.getString(Extra.ACCESS_KEY);

        int resultId = Apis.get()
                .vkDefault(accountId)
                .docs()
                .add(ownerId, id, accessKey)
                .blockingGet();

        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ID, resultId);
        return bundle;
    }
}

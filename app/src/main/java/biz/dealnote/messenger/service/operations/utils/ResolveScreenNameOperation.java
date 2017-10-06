package biz.dealnote.messenger.service.operations.utils;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import biz.dealnote.messenger.db.OwnersHelper;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Logger;

public class ResolveScreenNameOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        String name = request.getString(EXTRA_NAME);

        Logger.d(TAG, "from BD, start query");
        ResolveDomailResponse resolveDomailResult = OwnersHelper.resolveScreenName(context, accountId, name);
        Logger.d(TAG, "from BD, resolveDomailResult: " + resolveDomailResult);

        if (resolveDomailResult == null) {
            resolveDomailResult = Apis.get()
                    .vkDefault(accountId)
                    .utils()
                    .resolveScreenName(name)
                    .blockingGet();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.RESPONSE, resolveDomailResult);
        return bundle;
    }

}

package biz.dealnote.messenger.service.operations.message;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class MessagesRestoreOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);

        boolean result = Apis.get()
                .vkDefault(accountId)
                .messages()
                .restore(id)
                .blockingGet();

        return buildSimpleSuccessResult(result);
    }
}
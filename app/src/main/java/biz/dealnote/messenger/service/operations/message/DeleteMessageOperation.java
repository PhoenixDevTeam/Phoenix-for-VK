package biz.dealnote.messenger.service.operations.message;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.IntArray;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DeleteMessageOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        IntArray list = (IntArray) request.getParcelable(EXTRA_MESSAGE_IDS);
        Apis.get()
                .vkDefault(accountId)
                .messages()
                .delete(list.asList(), null)
                .blockingGet();
        return null;
    }
}

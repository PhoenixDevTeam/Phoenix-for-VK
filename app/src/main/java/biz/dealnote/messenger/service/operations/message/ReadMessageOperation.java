package biz.dealnote.messenger.service.operations.message;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.Collection;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.fragment.MessageHandler;
import biz.dealnote.messenger.service.IntArray;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Objects.isNull;

public class ReadMessageOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        IntArray ids = (IntArray) request.getParcelable(Extra.IDS);
        Integer peerId = request.optInt(Extra.PEER_ID);
        Integer startMessageId = request.optInt(EXTRA_START_ID);
        Collection<Integer> collection = isNull(ids) ? null : ids.asList();

        int count = MessageHandler.markMessagesAsRead(context, accountId, collection, peerId, startMessageId);

        Logger.d("ReadMessageOperation", "count=" + count);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .messages()
                .markAsRead(collection, peerId, startMessageId)
                .blockingGet();

        return buildSimpleSuccessResult(success);
    }
}

package biz.dealnote.messenger.service.operations.audio;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DeleteAudioOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);
        int ownerId = request.contains(Extra.OWNER_ID) ? request.getInt(Extra.OWNER_ID) : accountId;

        boolean success = Apis.get()
                .vkDefault(accountId)
                .audio()
                .delete(id, ownerId)
                .blockingGet();

        if (success) {
            //AudioHelper.markAsDeleted(context, accountId, id, ownerId, true);
        }

        return buildSimpleSuccessResult(success);
    }
}

package biz.dealnote.messenger.service.operations.audio;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class RestoreAudioOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);
        int ownerId = request.contains(Extra.OWNER_ID) ? request.getInt(Extra.OWNER_ID) : accountId;

        VKApiAudio restoredAudio = Apis.get()
                .vkDefault(accountId)
                .audio()
                .restore(id, ownerId)
                .blockingGet();

        if (restoredAudio != null) {

        }

        return buildSimpleSuccessResult(restoredAudio != null);
    }
}

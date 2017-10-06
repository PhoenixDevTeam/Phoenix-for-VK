package biz.dealnote.messenger.service.operations.audio;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.Arrays;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.service.IntArray;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Objects.isNull;


public class SetBroadcastAudioOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int audioId = request.getInt(Extra.ID);
        int ownerId = request.getInt(Extra.OWNER_ID);

        IntArray targetIds = (IntArray) request.getParcelable(Extra.IDS);

        int[] ids = Apis.get()
                .vkDefault(accountId)
                .audio()
                .setBroadcast(new IdPair(audioId, ownerId), isNull(targetIds) ? null : targetIds.asList())
                .blockingGet();

        Logger.d("SetBroadcastAudioOperation", "ids: " + Arrays.toString(ids));
        return buildSimpleSuccessResult(true);
    }
}
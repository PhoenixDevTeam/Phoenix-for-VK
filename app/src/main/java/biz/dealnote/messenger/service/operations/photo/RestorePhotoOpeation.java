package biz.dealnote.messenger.service.operations.photo;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class RestorePhotoOpeation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int ownerId = request.contains(Extra.OWNER_ID) ? request.getInt(Extra.OWNER_ID) : accountId;
        int photoId = request.getInt(Extra.PHOTO_ID);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .photos()
                .restore(ownerId, photoId)
                .blockingGet();

        return buildSimpleSuccessResult(success);
    }
}

package biz.dealnote.messenger.service.operations.photo;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class CopyPhotoOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int ownerId = request.getInt(Extra.OWNER_ID);
        int photoId = request.getInt(Extra.PHOTO_ID);
        String accessKey = request.getString(Extra.ACCESS_KEY);

        int id = Apis.get()
                .vkDefault(accountId)
                .photos()
                .copy(ownerId, photoId, accessKey)
                .blockingGet();

        Bundle bundle = new Bundle();
        bundle.putInt(Extra.PHOTO_ID, id);
        return bundle;
    }
}

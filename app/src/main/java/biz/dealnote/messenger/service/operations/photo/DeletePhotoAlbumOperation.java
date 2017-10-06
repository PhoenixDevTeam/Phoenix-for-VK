package biz.dealnote.messenger.service.operations.photo;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.Repositories;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DeletePhotoAlbumOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int albumId = request.getInt(Extra.ALBUM_ID);
        Integer groupId = request.contains(Extra.GROUP_ID) ? request.getInt(Extra.GROUP_ID) : null;

        boolean deleted = Apis.get()
                .vkDefault(accountId)
                .photos()
                .deleteAlbum(albumId, groupId)
                .blockingGet();

        if (deleted) {
            int ownerId = groupId == null ? accountId : -Math.abs(groupId);

            Repositories.getInstance()
                    .photoAlbums()
                    .removeAlbumById(accountId, ownerId, albumId)
                    .blockingAwait();
        }

        return buildSimpleSuccessResult(deleted);
    }
}

package biz.dealnote.messenger.service.operations.photo;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class CreatePhotoAlbumOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        /*String title = request.getString(Extra.TITLE);
        Integer groupId = request.optInt(Extra.GROUP_ID);
        String description = request.getString(EXTRA_DESCRIPTION);
        SimplePrivacy privacyView = (SimplePrivacy) request.getParcelable(EXTRA_PRIVACY_VIEW);
        SimplePrivacy privacyComment = (SimplePrivacy) request.getParcelable(EXTRA_PRIVACY_COMMENT);

        Boolean uploadByAdmins = request.optBoolean(EXTRA_UPLOAD_BY_ADMINS_ONLY);
        Boolean commentsDisable = request.optBoolean(EXTRA_COMMENTS_DISABLE);

        //VkApiPrivacy privacyViewDto = isNull(privacyView) ? null : privacyView.toDto();
        //VkApiPrivacy privacyCommentDto = isNull(privacyComment) ? null : privacyComment.toDto();

        VkApiPrivacy privacyViewDto = null;
        VkApiPrivacy privacyCommentDto = null;

        VKApiPhotoAlbum apiPhotoAlbum = Apis.get()
                .vkDefault(accountId)
                .photos()
                .createAlbum(title, groupId, description, privacyViewDto, privacyCommentDto, uploadByAdmins,
                        commentsDisable)
                .blockingGet();

        int ownerId = groupId == null ? accountId : -Math.abs(groupId);

        Repositories.getInstance()
                .photoAlbums()
                .store(accountId, ownerId, Collections.singletonList(apiPhotoAlbum), false)
                .blockingGet();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.ALBUM, apiPhotoAlbum);
        return bundle;*/

        return null;
    }
}

package biz.dealnote.messenger.service.operations.photo;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class EditPhotoAlbumOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        /*int albumId = request.getInt(Extra.ALBUM_ID);
        int ownerId = request.getInt(Extra.OWNER_ID);

        String title = request.getString(Extra.TITLE);
        String description = request.getString(EXTRA_DESCRIPTION);

        SimplePrivacy privacyView = (SimplePrivacy) request.getParcelable(EXTRA_PRIVACY_VIEW);
        SimplePrivacy privacyComment = (SimplePrivacy) request.getParcelable(EXTRA_PRIVACY_COMMENT);

        Boolean uploadByAdmins = request.optBoolean(EXTRA_UPLOAD_BY_ADMINS_ONLY);
        Boolean commentsDisable = request.optBoolean(EXTRA_COMMENTS_DISABLE);

        //VkApiPrivacy privacyViewDto = isNull(privacyView) ? null : privacyView.toDto();
        //VkApiPrivacy privacyCommentDto = isNull(privacyComment) ? null : privacyComment.toDto();

        VkApiPrivacy privacyViewDto = null;
        VkApiPrivacy privacyCommentDto = null;

        boolean success = Apis.get()
                .vkDefault(accountId)
                .photos()
                .editAlbum(albumId, title, description, ownerId, privacyViewDto, privacyCommentDto,
                        uploadByAdmins, commentsDisable)
                .blockingGet();

        //boolean success = api.editAlbum(albumId, title, description, ownerId,
        //        isNull(privacyView) ? null : privacyView.toDto(),
        //        isNull(privacyComment) ? null : privacyComment.toDto(),
        //        uploadByAdmins, commentsDisable);

        Bundle bundle = new Bundle();
        bundle.putBoolean(Extra.SUCCESS, success);

        if (success) {
            PhotoAlbum album = Repositories.getInstance()
                    .photoAlbums()
                    .findAlbumById(accountId, ownerId, albumId)
                    .blockingGet();

            if(Objects.nonNull(album)){
                album.setTitle(title);
                album.setDescription(description);
                album.setUploadByAdminsOnly(uploadByAdmins != null && uploadByAdmins);
                album.setCommentsDisabled(commentsDisable != null && commentsDisable);
                album.setPrivacyComment(privacyComment);
                album.setPrivacyView(privacyView);

                bundle.putParcelable(Extra.ALBUM, album);
            }

            // TODO: 06.09.2017

            //Repositories.getInstance()
            //        .photoAlbums()
            //        .store(accountId, ownerId, Collections.singletonList(album.toDto()), false)
            //        .blockingGet();
        }
        return bundle;
*/
        return null;
    }
}

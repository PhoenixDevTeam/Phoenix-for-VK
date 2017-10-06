package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.AccessIdPair;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPhotoAlbum;
import biz.dealnote.messenger.api.model.VkApiPrivacy;
import biz.dealnote.messenger.api.model.response.DefaultCommentsResponse;
import biz.dealnote.messenger.api.model.response.UploadOwnerPhotoResponse;
import biz.dealnote.messenger.api.model.server.VkApiOwnerPhotoUploadServer;
import biz.dealnote.messenger.api.model.server.VkApiPhotoMessageServer;
import biz.dealnote.messenger.api.model.server.VkApiUploadServer;
import biz.dealnote.messenger.api.model.server.VkApiWallUploadServer;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
public interface IPhotosApi {

    @CheckResult
    Single<Boolean> deleteAlbum(int albumId, Integer groupId);

    @CheckResult
    Single<Boolean> restore(Integer ownerId, int photoId);

    @CheckResult
    Single<Boolean> delete(Integer ownerId, int photoId);

    @CheckResult
    Single<Boolean> deleteComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> restoreComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> editComment(Integer ownerId, int commentId, String message,
                                Collection<IAttachmentToken> attachments);

    @CheckResult
    Single<VKApiPhotoAlbum> createAlbum(String title, Integer groupId, String description,
                                        VkApiPrivacy privacyView, VkApiPrivacy privacyComment,
                                        Boolean uploadByAdminsOnly, Boolean commentsDisabled);

    @CheckResult
    Single<Boolean> editAlbum(int albumId, String title, String description, Integer ownerId,
                              VkApiPrivacy privacyView, VkApiPrivacy privacyComment,
                              Boolean uploadByAdminsOnly, Boolean commentsDisabled);

    @CheckResult
    Single<Integer> copy(int ownerId, int photoId, String accessKey);

    @CheckResult
    Single<Integer> createComment(Integer ownerId, int photoId, Boolean fromGroup, String message,
                                  Integer replyToComment, Collection<IAttachmentToken> attachments,
                                  Integer stickerId, String accessKey, Integer generatedUniqueId);

    @CheckResult
    Single<DefaultCommentsResponse> getComments(Integer ownerId, int photoId, Boolean needLikes,
                                                Integer startCommentId, Integer offset, Integer count, String sort,
                                                String accessKey, Boolean extended, String fields);

    @CheckResult
    Single<List<VKApiPhoto>> getById(@NonNull Collection<AccessIdPair> ids);

    @CheckResult
    Single<VkApiUploadServer> getUploadServer(int albumId, Integer groupId);

    @CheckResult
    Single<UploadOwnerPhotoResponse> saveOwnerPhoto(String server, String hash, String photo);

    @CheckResult
    Single<VkApiOwnerPhotoUploadServer> getOwnerPhotoUploadServer(Integer ownerId);

    @CheckResult
    Single<List<VKApiPhoto>> saveWallPhoto(Integer userId, Integer groupId, String photo, int server,
                                           String hash, Double latitude, Double longitude, String caption);

    @CheckResult
    Single<VkApiWallUploadServer> getWallUploadServer(Integer groupId);

    @CheckResult
    Single<List<VKApiPhoto>> save(int albumId, Integer groupId, int server, String photosList, String hash,
                                  Double latitude, Double longitude, String caption);

    @CheckResult
    Single<Items<VKApiPhoto>> get(Integer ownerId, String albumId, Collection<Integer> photoIds, Boolean rev,
                                  Integer offset, Integer count);

    @CheckResult
    Single<VkApiPhotoMessageServer> getMessagesUploadServer();

    @CheckResult
    Single<List<VKApiPhoto>> saveMessagesPhoto(Integer server, String photo, String hash);

    @CheckResult
    Single<Items<VKApiPhotoAlbum>> getAlbums(Integer ownerId, Collection<Integer> albumIds, Integer offset,
                                             Integer count, Boolean needSystem, Boolean needCovers);

}

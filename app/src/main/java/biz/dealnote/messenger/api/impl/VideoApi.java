package biz.dealnote.messenger.api.impl;

import java.util.Collection;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IVideoApi;
import biz.dealnote.messenger.api.model.AccessIdPair;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.VKApiVideoAlbum;
import biz.dealnote.messenger.api.model.response.DefaultCommentsResponse;
import biz.dealnote.messenger.api.model.response.SearchVideoResponse;
import biz.dealnote.messenger.api.services.IVideoService;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
class VideoApi extends AbsApi implements IVideoApi {

    VideoApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<DefaultCommentsResponse> getComments(Integer ownerId, int videoId, Boolean needLikes, Integer startCommentId, Integer offset,
                                                       Integer count, String sort, Boolean extended, String fields) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service
                        .getComments(ownerId, videoId, integerFromBoolean(needLikes), startCommentId, offset, count, sort, integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> addVideo(Integer targetId, Integer videoId, Integer ownerId) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.addVideo(targetId, videoId, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deleteVideo(Integer videoId, Integer ownerId, Integer targetId) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.deleteVideo(videoId, ownerId, targetId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiVideoAlbum>> getAlbums(Integer ownerId, Integer offset, Integer count, Boolean needSystem) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.getAlbums(ownerId, offset, count, 1, integerFromBoolean(needSystem))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<SearchVideoResponse> search(String query, Integer sort, Boolean hd, Boolean adult,
                                              String filters, Boolean searchOwn, Integer offset,
                                              Integer longer, Integer shorter, Integer count, Boolean extended) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(query, sort, integerFromBoolean(hd), integerFromBoolean(adult), filters,
                                integerFromBoolean(searchOwn), offset, longer, shorter, count, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> restoreComment(Integer ownerId, int commentId) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.restoreComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> deleteComment(Integer ownerId, int commentId) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.deleteComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Items<VKApiVideo>> get(Integer ownerId, Collection<AccessIdPair> ids, Integer albumId,
                                         Integer count, Integer offset, Boolean extended) {
        String videos = join(ids, ",", AccessIdPair::format);
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.get(ownerId, videos, albumId, count, offset, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> createComment(Integer ownerId, int videoId, String message,
                                         Collection<IAttachmentToken> attachments, Boolean fromGroup,
                                         Integer replyToComment, Integer stickerId, Integer uniqueGeneratedId) {
        String atts = join(attachments, ",", AbsApi::formatAttachmentToken);
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service
                        .createComment(ownerId, videoId, message, atts, integerFromBoolean(fromGroup),
                                replyToComment, stickerId, uniqueGeneratedId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> editComment(Integer ownerId, int commentId, String message,
                                       Collection<IAttachmentToken> attachments) {
        return provideService(IVideoService.class, TokenType.USER)
                .flatMap(service -> service.editComment(ownerId, commentId, message, join(attachments, ",", AbsApi::formatAttachmentToken))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}

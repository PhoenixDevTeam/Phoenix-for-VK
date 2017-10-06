package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.interfaces.IFaveApi;
import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.FavePostsResponse;
import biz.dealnote.messenger.api.services.IFaveService;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class FaveApi extends AbsApi implements IFaveApi {

    FaveApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<VKApiUser>> getUsers(Integer offset, Integer count, String fields) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getUsers(offset, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPhotos(offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, Boolean extended) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getVideos(offset, count, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<FavePostsResponse> getPosts(Integer offset, Integer count, Boolean extended) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getPosts(offset, count, integerFromBoolean(extended))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<FaveLinkDto>> getLinks(Integer offset, Integer count) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.getLinks(offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> addGroup(int groupId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addGroup(groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addUser(int userId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.addUser(userId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeUser(int userId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeUser(userId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> removeLink(String linkId) {
        return provideService(IFaveService.class)
                .flatMap(service -> service.removeLink(linkId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}
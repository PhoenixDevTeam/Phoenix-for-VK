package biz.dealnote.messenger.api.impl;

import java.util.Collection;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.interfaces.IAudioApi;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.services.IAudioService;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class AudioApi extends AbsApi implements IAudioApi {

    AudioApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<int[]> setBroadcast(IdPair audio, Collection<Integer> targetIds) {
        String audioStr = Objects.isNull(audio) ? null : audio.ownerId + "_" + audio.id;
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .setBroadcast(audioStr, join(targetIds, ","))
                        .map(extractResponseWithErrorHandling()));

    }

    @Override
    public Single<Items<VKApiAudio>> search(String query, Boolean autoComplete, Boolean lyrics, Boolean performerOnly, Integer sort, Boolean searchOwn, Integer offset, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .search(query, integerFromBoolean(autoComplete), integerFromBoolean(lyrics),
                                integerFromBoolean(performerOnly), sort, integerFromBoolean(searchOwn), offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudio> restore(int audioId, Integer ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .restore(audioId, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> delete(int audioId, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .delete(audioId, ownerId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> add(int audioId, int ownerId, Integer groupId, Integer albumId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .add(audioId, ownerId, groupId, albumId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> get(Integer ownerId, Integer albumId, Collection<Integer> audioIds, Integer offset, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .get(ownerId, albumId, join(audioIds, ","), 0, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }
}
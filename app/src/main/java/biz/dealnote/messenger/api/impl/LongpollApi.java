package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IOtherVkRetrofitProvider;
import biz.dealnote.messenger.api.interfaces.ILongpollApi;
import biz.dealnote.messenger.api.model.longpoll.VkApiGroupLongpollUpdates;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.api.services.ILongpollUpdatesService;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class LongpollApi implements ILongpollApi {

    private final IOtherVkRetrofitProvider provider;

    LongpollApi(IOtherVkRetrofitProvider provider) {
        this.provider = provider;
    }

    @Override
    public Single<VkApiLongpollUpdates> getUpdates(String server, String key, long ts, int wait, int mode, int version) {
        return provider.provideLongpollRetrofit()
                .flatMap(wrapper -> wrapper.create(ILongpollUpdatesService.class)
                        .getUpdates(server, "a_check", key, ts, wait, mode, version));
    }

    @Override
    public Single<VkApiGroupLongpollUpdates> getGroupUpdates(String server, String key, String ts, int wait) {
        return provider.provideLongpollRetrofit()
                .flatMap(wrapper -> wrapper.create(ILongpollUpdatesService.class)
                        .getGroupUpdates(server, "a_check", key, ts, wait));
    }
}
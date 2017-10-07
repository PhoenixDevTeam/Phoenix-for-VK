package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IOtherVkRetrofitProvider;
import biz.dealnote.messenger.api.IUploadRetrofitProvider;
import biz.dealnote.messenger.api.IVkRetrofitProvider;
import biz.dealnote.messenger.api.OtherVkRetrofitProvider;
import biz.dealnote.messenger.api.UploadRetrofitProvider;
import biz.dealnote.messenger.api.VkMethodHttpClientFactory;
import biz.dealnote.messenger.api.VkRetrofitProvider;
import biz.dealnote.messenger.api.interfaces.IAccountApis;
import biz.dealnote.messenger.api.interfaces.IAuthApi;
import biz.dealnote.messenger.api.interfaces.ILongpollApi;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.interfaces.IUploadApi;
import biz.dealnote.messenger.api.services.IAuthService;
import biz.dealnote.messenger.settings.IProxySettings;

/**
 * Created by ruslan.kolbasa on 30.12.2016.
 * phoenix
 */
public class Networker implements INetworker {

    private final IOtherVkRetrofitProvider otherVkRetrofitProvider;
    private final IVkRetrofitProvider vkRetrofitProvider;
    private final IUploadRetrofitProvider uploadRetrofitProvider;

    public Networker(IProxySettings settings) {
        this.otherVkRetrofitProvider = new OtherVkRetrofitProvider(settings);
        this.vkRetrofitProvider = new VkRetrofitProvider(settings, new VkMethodHttpClientFactory());
        this.uploadRetrofitProvider = new UploadRetrofitProvider(settings);
    }

    @Override
    public IAccountApis vkDefault(int accountId) {
        return VkApies.get(accountId, vkRetrofitProvider);
    }

    @Override
    public IAccountApis vkManual(int accountId, String accessToken) {
        return VkApies.create(accountId, accessToken, vkRetrofitProvider);
    }

    @Override
    public IAuthApi vkDirectAuth() {
        return new AuthApi(() -> otherVkRetrofitProvider.provideAuthRetrofit().map(wrapper -> wrapper.create(IAuthService.class)));
    }

    @Override
    public ILongpollApi longpoll() {
        return new LongpollApi(otherVkRetrofitProvider);
    }

    @Override
    public IUploadApi uploads() {
        return new UploadApi(uploadRetrofitProvider);
    }
}
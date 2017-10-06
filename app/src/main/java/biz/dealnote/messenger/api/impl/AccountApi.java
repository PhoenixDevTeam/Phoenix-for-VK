package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IAccountApi;
import biz.dealnote.messenger.api.model.CountersDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.services.IAccountService;
import io.reactivex.Single;

/**
 * Created by admin on 04.01.2017.
 * phoenix
 */
class AccountApi extends AbsApi implements IAccountApi {

    AccountApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Integer> banUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .banUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> unbanUser(int userId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .unbanUser(userId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiUser>> getBanned(Integer count, Integer offset, String fields) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getBanned(count, offset, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> unregisterDevice(String deviceId) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service.unregisterDevice(deviceId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> registerDevice(String token, String deviceModel, Integer deviceYear,
                                          String deviceId, String systemVersion, String settings) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .registerDevice(token, deviceModel, deviceYear, deviceId, systemVersion, settings)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setOffline() {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .setOffline()
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setOnline(Boolean voip) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service.setOnline(integerFromBoolean(voip))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<CountersDto> getCounters(String filter) {
        return provideService(IAccountService.class, TokenType.USER)
                .flatMap(service -> service
                        .getCounters(filter)
                        .map(extractResponseWithErrorHandling()));
    }
}
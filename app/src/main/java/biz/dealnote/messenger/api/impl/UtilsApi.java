package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IUtilsApi;
import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import biz.dealnote.messenger.api.services.IUtilsService;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
class UtilsApi extends AbsApi implements IUtilsApi {

    UtilsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<ResolveDomailResponse> resolveScreenName(String screenName) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.resolveScreenName(screenName)
                .map(extractResponseWithErrorHandling()));
    }
}
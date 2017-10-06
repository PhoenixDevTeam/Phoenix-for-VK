package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IPagesApi;
import biz.dealnote.messenger.api.model.VKApiWikiPage;
import biz.dealnote.messenger.api.services.IPagesService;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
class PagesApi extends AbsApi implements IPagesApi {

    PagesApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VKApiWikiPage> get(int ownerId, int pageId, Boolean global, Boolean sitePreview, String title, Boolean needSource, Boolean needHtml) {
        return provideService(IPagesService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(ownerId, pageId, integerFromBoolean(global), integerFromBoolean(sitePreview),
                                title, integerFromBoolean(needSource), integerFromBoolean(needHtml))
                        .map(extractResponseWithErrorHandling()));
    }
}

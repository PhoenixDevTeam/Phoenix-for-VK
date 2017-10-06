package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.INotificationsApi;
import biz.dealnote.messenger.api.model.feedback.VkApiBaseFeedback;
import biz.dealnote.messenger.api.model.response.NotificationsResponse;
import biz.dealnote.messenger.api.services.INotificationsService;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
class NotificationsApi extends AbsApi implements INotificationsApi {

    NotificationsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Integer> markAsViewed() {
        return provideService(INotificationsService.class, TokenType.USER)
                .flatMap(service -> service.markAsViewed()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NotificationsResponse> get(Integer count, String startFrom, String filters, Long startTime, Long endTime) {
        return provideService(INotificationsService.class, TokenType.USER)
                .flatMap(service -> service.get(count, startFrom, filters, startTime, endTime)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            if (nonNull(response.notifications)) {
                                for (VkApiBaseFeedback n : response.notifications) {
                                    if (nonNull(n.reply)) {
                                        // fix В ответе нет этого параметра
                                        n.reply.from_id = getAccountId();
                                    }
                                }
                            }
                            return response;
                        }));
    }
}
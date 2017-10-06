package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.NotificationsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public interface INotificationsApi {

    @CheckResult
    Single<Integer> markAsViewed();

    @CheckResult
    Single<NotificationsResponse> get(Integer count, String startFrom, String filters,
                                                    Long startTime, Long endTime);

}

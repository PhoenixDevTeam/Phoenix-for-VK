package biz.dealnote.messenger.api;

import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 16.06.2017.
 * phoenix
 */
public interface IServiceProvider {
    <T> Single<T> provideService(int accountId, Class<T> serviceClass, int ... tokenTypes);
}
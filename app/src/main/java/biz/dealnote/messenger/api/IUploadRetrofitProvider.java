package biz.dealnote.messenger.api;

import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IUploadRetrofitProvider {
    Single<RetrofitWrapper> provideUploadRetrofit();
}
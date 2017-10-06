package biz.dealnote.messenger.api;

import biz.dealnote.messenger.api.services.IAuthService;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 28.07.2017.
 * phoenix
 */
public interface IDirectLoginSeviceProvider {
    Single<IAuthService> provideAuthService();
}
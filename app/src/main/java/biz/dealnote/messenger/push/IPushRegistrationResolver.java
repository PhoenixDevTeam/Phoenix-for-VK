package biz.dealnote.messenger.push;

import io.reactivex.Completable;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IPushRegistrationResolver {
    boolean canReceivePushNotification();
    Completable resolvePushRegistration();
}
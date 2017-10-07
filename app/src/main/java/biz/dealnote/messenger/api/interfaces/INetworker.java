package biz.dealnote.messenger.api.interfaces;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
public interface INetworker {

    IAccountApis vkDefault(int accountId);

    IAccountApis vkManual(int accountId, String accessToken);

    IAuthApi vkDirectAuth();

    ILongpollApi longpoll();

    IUploadApi uploads();
}
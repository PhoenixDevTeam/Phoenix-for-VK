package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface ILongpollApi {
    Single<VkApiLongpollUpdates> getUpdates(String server, String key, long ts, int wait, int mode, int version);
}
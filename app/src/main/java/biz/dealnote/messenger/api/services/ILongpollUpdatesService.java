package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by ruslan.kolbasa on 23.12.2016.
 * phoenix
 */
public interface ILongpollUpdatesService {

    @GET
    Single<VkApiLongpollUpdates> getUpdates(@Url String server, @Query("act") String act,
                                            @Query("key") String key,
                                            @Query("ts") long ts,
                                            @Query("wait") int wait,
                                            @Query("mode") int mode,
                                            @Query("version") int version);

}

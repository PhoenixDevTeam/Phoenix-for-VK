package biz.dealnote.messenger.longpoll;

import java.util.List;

import biz.dealnote.messenger.longpoll.model.AbsRealtimeAction;
import io.reactivex.Flowable;

public interface ILongpollManager {
    void forceDestroy(int accountId);

    Flowable<List<AbsRealtimeAction>> observe();

    Flowable<Integer> observeKeepAlive();

    void keepAlive(int accountId);
}
package biz.dealnote.messenger.longpoll;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.longpoll.VkApiGroupLongpollUpdates;
import biz.dealnote.messenger.api.model.response.GroupLongpollServer;
import biz.dealnote.messenger.util.PersistentLogger;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

class GroupLongpoll implements ILongpoll {

    private static final int DELAY_ON_ERROR = 10 * 1000;

    private final int groupId;
    private String key;
    private String server;
    private String ts;
    private Callback callback;
    private final INetworker networker;

    GroupLongpoll(INetworker networker, int groupId, Callback callback) {
        this.groupId = groupId;
        this.callback = callback;
        this.networker = networker;
    }

    @Override
    public int getAccountId() {
        return -groupId;
    }

    private void resetServerAttrs() {
        this.server = null;
        this.key = null;
        this.ts = null;
    }

    @Override
    public void shutdown() {
        compositeDisposable.dispose();
    }

    @Override
    public void connect() {
        if (!isListeningNow()) {
            get();
        }
    }

    private boolean isListeningNow(){
        return compositeDisposable.size() > 0;
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private void onServerInfoReceived(GroupLongpollServer info) {
        this.ts = info.ts;
        this.key = info.key;
        this.server = info.server;

        get();
    }

    private void onServerGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, ServerGet", throwable);
        getWithDelay();
    }

    private void get() {
        compositeDisposable.clear();

        boolean validServer = nonEmpty(server) && nonEmpty(key) && nonEmpty(ts);
        if (validServer) {
            compositeDisposable.add(networker.longpoll()
                    .getGroupUpdates(server, key, ts, 25)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onUpdates, this::onUpdatesGetError));
        } else {
            compositeDisposable.add(networker.vkDefault(getAccountId())
                    .groups()
                    .getLongPollServer(groupId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onServerInfoReceived, this::onServerGetError));
        }
    }

    private void onUpdates(VkApiGroupLongpollUpdates updates) {
        if (updates.failed > 0) {
            resetServerAttrs();
            getWithDelay();
        } else {
            ts = updates.ts;

            if (updates.getCount() > 0) {
                callback.onUpdates(groupId, updates);
            }

            get();
        }
    }

    private void onUpdatesGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, UpdatesGet", throwable);
        getWithDelay();
    }

    private Observable<Long> delayedObservable = Observable.interval(DELAY_ON_ERROR, DELAY_ON_ERROR,
            TimeUnit.MILLISECONDS, Injection.provideMainThreadScheduler());

    private void getWithDelay() {
        compositeDisposable.add(delayedObservable.subscribe(o -> get()));
    }

    public interface Callback {
        void onUpdates(int groupId, @NonNull VkApiGroupLongpollUpdates updates);
    }
}
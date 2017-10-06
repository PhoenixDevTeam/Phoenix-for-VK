package biz.dealnote.messenger.longpoll;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VkApiLongpollServer;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.PersistentLogger;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

class Longpoll {

    private static final String TAG = "Longpoll_TAG";
    private static final int DELAY_ON_ERROR = 10 * 1000;

    private Context context;
    private final int accountId;
    private String key;
    private String server;
    private Long ts;
    private Callback callback;
    private final INetworker networker;

    Longpoll(Context context, INetworker networker, int accountId, Callback callback) {
        this.context = context.getApplicationContext();
        this.accountId = accountId;
        this.callback = callback;
        this.networker = networker;
    }

    private void resetServerAttrs() {
        this.server = null;
        this.key = null;
        this.ts = null;
    }

    void shutdown() {
        Logger.d(TAG, "shutdown, aid: " + accountId);
        resetUpdatesDisposable();
    }

    void connect() {
        Logger.d(TAG, "connect, aid: " + accountId);
        if (!isListeningNow()) {
            get();
        }
    }

    private boolean isListeningNow() {
        return nonNull(mCurrentUpdatesDisposable) && !mCurrentUpdatesDisposable.isDisposed();
    }

    private Disposable mCurrentUpdatesDisposable;

    private void resetUpdatesDisposable() {
        if (nonNull(mCurrentUpdatesDisposable)) {
            if (!mCurrentUpdatesDisposable.isDisposed()) {
                mCurrentUpdatesDisposable.dispose();
            }

            mCurrentUpdatesDisposable = null;
        }
    }

    private void onServerInfoReceived(VkApiLongpollServer info) {
        Logger.d(TAG, "onResponse, info: " + info);

        this.ts = info.ts;
        this.key = info.key;
        this.server = info.server;

        get();
    }

    private void onServerGetError(Throwable throwable) {
        throwable.printStackTrace();

        PersistentLogger.logThrowable("Longpoll, ServerGet", throwable);

        getWithDelay();
    }

    private void get() {
        Logger.d(TAG, "get");

        resetUpdatesDisposable();

        if (!isNetworkAvailable()) {
            Logger.d(TAG, "no internet connection");

            getWithDelay();
            return;
        }

        boolean serverIsValid = nonEmpty(server) && nonEmpty(key) && nonNull(ts);

        if (!serverIsValid) {
            setDisposable(networker.vkDefault(accountId)
                    .messages()
                    .getLongpollServer(true, true)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onServerInfoReceived, this::onServerGetError));
            return;
        }

        setDisposable(networker.longpoll()
                .getUpdates("https://" + server, key, ts, 25, MODE, 1)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUpdates, this::onUpdatesGetError));
    }

    private void setDisposable(Disposable disposable) {
        mCurrentUpdatesDisposable = disposable;
    }

    private void onUpdates(VkApiLongpollUpdates updates) {
        Logger.d(TAG, "onUpdates, updates: " + updates);

        if (updates.failed > 0) {
            resetServerAttrs();
            getWithDelay();
        } else {
            ts = updates.ts;

            if (updates.getUpdatesCount() > 0) {
                callback.onUpdates(accountId, updates);
            }

            get();
        }
    }

    private void onUpdatesGetError(Throwable throwable) {
        Logger.d(TAG, "onUpdatesGetError, throwable: " + throwable);

        throwable.printStackTrace();

        PersistentLogger.logThrowable("Longpoll, UpdatesGet", throwable);

        getWithDelay();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private Observable<Long> mDelayedObservable = Observable.interval(DELAY_ON_ERROR, DELAY_ON_ERROR,
            TimeUnit.MILLISECONDS, Injection.provideMainThreadScheduler());

    private void getWithDelay() {
        setDisposable(mDelayedObservable.subscribe(o -> get()));
    }

    private static final int MODE =
            2 + //получать вложения;
                    32 + //возвращать pts (это требуется для работы метода messages.getLongPollHistory без ограничения в 256 последних событий);
                    64 + //в событии с кодом 8 (друг стал онлайн) возвращать дополнительные данные в поле $extra (подробнее в разделе Структура событий);
                    128; //возвращать с сообщением параметр random_id (random_id может быть передан при отправке сообщения методом messages.send).

    public interface Callback {
        void onUpdates(int aid, VkApiLongpollUpdates updates);
    }
}

package biz.dealnote.messenger.task;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

public class TextingNotifier {

    private int accountId;
    private long lastNotifyTime;
    private boolean isRequestNow;
    private Disposable disposable;

    public TextingNotifier(int accountId) {
        this.accountId = accountId;
    }

    private static Completable createNotifier(int accountId, int peerId) {
        return Apis.get()
                .vkDefault(accountId)
                .messages()
                .setActivity(peerId, true)
                .delay(5, TimeUnit.SECONDS)
                .toCompletable();
    }

    public void notifyAboutTyping(int peerId) {
        if (!canNotifyNow()) {
            return;
        }

        lastNotifyTime = System.currentTimeMillis();

        isRequestNow = true;
        disposable = createNotifier(accountId, peerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> isRequestNow = false, ignored -> isRequestNow = false);
    }

    public void shutdown(){
        if(disposable != null){
            if(!disposable.isDisposed()){
                disposable.dispose();
            }

            disposable = null;
        }
    }

    private boolean canNotifyNow() {
        return !isRequestNow && Math.abs(System.currentTimeMillis() - lastNotifyTime) > 5000;
    }
}
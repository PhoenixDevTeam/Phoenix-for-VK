package biz.dealnote.messenger;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.service.KeepLongpollService;
import biz.dealnote.messenger.settings.Settings;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.longpoll.NotificationHelper.tryCancelNotificationForPeer;

public class App extends Application {

    //noinspection ResourceType
    private static App sInstanse;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        sInstanse = this;
        AppCompatDelegate.setDefaultNightMode(Settings.get().ui().getNightMode());

        super.onCreate();

        Settings.get()
                .main()
                .incrementRunCount();

        PicassoInstance.init(this, Injection.provideProxySettings());

        if (Settings.get().other().isKeepLongpoll()) {
            KeepLongpollService.start(this);
        }

        compositeDisposable.add(Repository.INSTANCE.getMessages()
                .observePeerUpdates()
                .flatMap(Flowable::fromIterable)
                .subscribe(update -> {
                    if (update.getReadOut() != null) {
                        tryCancelNotificationForPeer(App.this, update.getAccountId(), update.getPeerId());
                    }
                }));

        compositeDisposable.add(Repository.INSTANCE.getMessages()
                .observeSentMessages()
                .subscribe(sentMsg -> tryCancelNotificationForPeer(App.this, sentMsg.getAccountId(), sentMsg.getPeerId())));
    }

    @NonNull
    public static App getInstance() {
        if (sInstanse == null) {
            throw new IllegalStateException("App instance is null!!! WTF???");
        }

        return sInstanse;
    }
}
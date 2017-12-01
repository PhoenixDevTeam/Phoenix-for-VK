package biz.dealnote.messenger;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.settings.Settings;

public class App extends BaseApp {

    //noinspection ResourceType
    private static App sInstanse;

    @Override
    public void onCreate() {
        sInstanse = this;
        super.onCreate();

        Settings.get()
                .main()
                .incrementRunCount();

        PicassoInstance.init(this, Injection.provideProxySettings());
    }

    @NonNull
    public static App getInstance() {
        if (sInstanse == null) {
            throw new IllegalStateException("App instance is null!!! WTF???");
        }

        return sInstanse;
    }
}
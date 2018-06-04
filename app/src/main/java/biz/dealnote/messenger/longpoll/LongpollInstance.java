package biz.dealnote.messenger.longpoll;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.realtime.Processors;

public class LongpollInstance {

    private static volatile ILongpollManager longpollManager;

    public static ILongpollManager get() {
        if(longpollManager == null){
            synchronized (LongpollInstance.class){
                if(longpollManager == null){
                    longpollManager = new AndroidLongpollManager(App.getInstance(), Injection.provideNetworkInterfaces(), Processors.realtimeMessages());
                }
            }
        }
        return longpollManager;
    }
}
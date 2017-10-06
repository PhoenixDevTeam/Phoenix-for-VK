package biz.dealnote.messenger.realtime;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by admin on 11.04.2017.
 * phoenix
 */
public final class Processors {

    private static IRealtimeMessagesProcessor realtimeMessagesProcessor;

    public static IRealtimeMessagesProcessor realtimeMessages() {
        if(isNull(realtimeMessagesProcessor)){
            synchronized (Processors.class){
                if(isNull(realtimeMessagesProcessor)){
                    realtimeMessagesProcessor = new RealtimeMessagesProcessor();
                }
            }
        }
        return realtimeMessagesProcessor;
    }
}
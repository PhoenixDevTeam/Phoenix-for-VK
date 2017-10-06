package biz.dealnote.messenger.api;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
public class Apis {

    public static INetworker get(){
        return Injection.provideNetworkInterfaces();
    }

}

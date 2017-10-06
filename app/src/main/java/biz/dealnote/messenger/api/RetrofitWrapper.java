package biz.dealnote.messenger.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;

/**
 * Created by admin on 27.07.2017.
 * phoenix
 */
public class RetrofitWrapper {

    private final Retrofit retrofit;
    private Map<Class<?>, Object> servicesCache;
    private final boolean withCaching;

    private RetrofitWrapper(Retrofit retrofit, boolean withCaching) {
        this.retrofit = retrofit;
        this.withCaching = withCaching;
        this.servicesCache = withCaching ? Collections.synchronizedMap(new HashMap<>(4)) : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> serviceClass) {
        if (!withCaching) {
            return retrofit.create(serviceClass);
        }

        if (servicesCache.containsKey(serviceClass)) {
            return (T) servicesCache.get(serviceClass);
        }

        T service = retrofit.create(serviceClass);
        servicesCache.put(serviceClass, service);
        return service;
    }

    public void cleanup(){
        servicesCache.clear();
    }

    public static RetrofitWrapper wrap(Retrofit retrofit) {
        return new RetrofitWrapper(retrofit, true);
    }

    public static RetrofitWrapper wrap(Retrofit retrofit, boolean withCaching) {
        return new RetrofitWrapper(retrofit, withCaching);
    }
}
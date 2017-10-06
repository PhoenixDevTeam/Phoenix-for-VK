package biz.dealnote.messenger.service;

import android.content.Context;

import com.foxykeep.datadroid.requestmanager.RequestManager;

public final class RestRequestManager extends RequestManager {

    // Singleton management
    private static RestRequestManager sInstance;

    public synchronized static RestRequestManager from(Context context) {
        if (sInstance == null) {
            sInstance = new RestRequestManager(context);
        }

        return sInstance;
    }

    private RestRequestManager(Context context) {
        super(context, RestService.class);
    }
}
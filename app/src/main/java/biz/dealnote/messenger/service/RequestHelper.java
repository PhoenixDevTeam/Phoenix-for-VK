package biz.dealnote.messenger.service;

import android.content.Context;

import com.foxykeep.datadroid.requestmanager.RequestManager;

import biz.dealnote.messenger.service.factory.AccountRequestFactory;

public class RequestHelper {

    private static final RequestManager.RequestListener DUMMY = new ServiceRequestAdapter();

    public static void checkPushRegistration(Context context){
        RestRequestManager.from(context).execute(AccountRequestFactory.getResolvePushRegistrationRequest(), DUMMY);
    }
}

package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

public class AccountRequestFactory {

    public static final int REQUEST_RESOLVE_PUSH_REGISTRATION = 21001;

    public static Request getResolvePushRegistrationRequest(){
        return new Request(REQUEST_RESOLVE_PUSH_REGISTRATION);
    }

}

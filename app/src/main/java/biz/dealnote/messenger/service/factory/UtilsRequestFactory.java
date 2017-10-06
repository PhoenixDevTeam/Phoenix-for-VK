package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class UtilsRequestFactory {

    public static final int REQUEST_SCREEN_NAME = 17001;

    public static Request getResolveScreenNameRequest(String name){
        Request request = new Request(REQUEST_SCREEN_NAME);
        request.put(AbsApiOperation.EXTRA_NAME, name);
        return request;
    }
}

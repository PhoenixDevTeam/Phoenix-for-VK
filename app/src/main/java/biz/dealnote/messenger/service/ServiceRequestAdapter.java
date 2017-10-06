package biz.dealnote.messenger.service;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager;

import biz.dealnote.messenger.exception.OtherServiceException;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;

public class ServiceRequestAdapter implements RequestManager.RequestListener {

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {

    }

    @Override
    public final void onRequestDataError(Request request) {
        onRequestError(request, new OtherServiceException("Data error"));
    }

    @Override
    public final void onRequestCustomError(Request request, Bundle resultData) {
        if(Objects.nonNull(resultData) && resultData.containsKey(RestService.ARGUMENT_NEW_SERVICE_ERROR)){
            Bundle serviceErrorBundle = resultData.getBundle(RestService.ARGUMENT_NEW_SERVICE_ERROR);
            AssertUtils.requireNonNull(serviceErrorBundle);

            ServiceException exception = ServiceException.deserializeFromBundle(serviceErrorBundle);
            onRequestError(request, exception);
        }
    }

    public void onRequestError(@NonNull Request request, @NonNull ServiceException exception){

    }
}

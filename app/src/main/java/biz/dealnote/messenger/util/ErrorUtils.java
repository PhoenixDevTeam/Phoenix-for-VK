package biz.dealnote.messenger.util;

import android.content.Context;

import biz.dealnote.messenger.api.ApiException;
import biz.dealnote.messenger.api.model.Error;
import biz.dealnote.messenger.service.ErrorLocalizer;

/**
 * Created by Ruslan Kolbasa on 17.05.2017.
 * phoenix
 */
public class ErrorUtils {

    public static String getLocalizedErrorMessage(Context context, Throwable throwable){
        throwable = Utils.getCauseIfRuntime(throwable);

        if (throwable instanceof ApiException) {
            Error error = ((ApiException) throwable).getError();
            return showApiError(context, error.errorCode, error.errorMsg);
        } else {
            return throwable.getMessage();
        }
    }

    private static String showApiError(Context context, int code, String rawMessage){
        return ErrorLocalizer.api().getMessage(context, code, rawMessage);
    }
}
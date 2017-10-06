package biz.dealnote.messenger.fragment.base;

import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

/**
 * Created by ruslan.kolbasa on 16-Jun-16.
 * phoenix
 */
interface RequestInterceptor {
    boolean intercept(Request request, Bundle resultData);
}

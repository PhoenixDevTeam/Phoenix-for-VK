package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IUtilsApi {

    @CheckResult
    Single<ResolveDomailResponse> resolveScreenName(String screenName);

}

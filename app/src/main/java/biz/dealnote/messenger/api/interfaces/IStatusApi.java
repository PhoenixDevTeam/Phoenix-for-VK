package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStatusApi {

    @CheckResult
    Single<Boolean> set(String text, Integer groupId);

}

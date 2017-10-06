package biz.dealnote.messenger.mvp.view;

import android.support.annotation.StringRes;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public interface IToastView {
    void showToast(@StringRes int titleTes, boolean isLong, Object... params);
}

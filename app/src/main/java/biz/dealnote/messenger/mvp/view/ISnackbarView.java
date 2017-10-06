package biz.dealnote.messenger.mvp.view;

import android.support.annotation.StringRes;

/**
 * Created by admin on 26.03.2017.
 * phoenix
 */
public interface ISnackbarView {
    void showSnackbar(@StringRes int res, boolean isLong);
}

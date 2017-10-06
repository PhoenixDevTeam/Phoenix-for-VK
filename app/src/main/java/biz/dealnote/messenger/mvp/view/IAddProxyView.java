package biz.dealnote.messenger.mvp.view;

import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 11.07.2017.
 * phoenix
 */
public interface IAddProxyView extends IMvpView, IErrorView {
    void setAuthFieldsEnabled(boolean enabled);
    void setAuthChecked(boolean checked);

    void goBack();
}

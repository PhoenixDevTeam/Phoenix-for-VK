package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 05.07.2017.
 * phoenix
 */
public interface IRequestExecuteView extends IMvpView, IErrorView, IProgressView, IAccountDependencyView, IToastView {
    void displayBody(String body);
    void hideKeyboard();
    void requestWriteExternalStoragePermission();
}
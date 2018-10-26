package biz.dealnote.messenger.fragment.base;

import android.widget.Toast;

import androidx.annotation.StringRes;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.IToastView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.compat.AbsMvpDialogFragment;
import biz.dealnote.mvp.core.AbsPresenter;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public abstract class BaseMvpDialogFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpDialogFragment<P, V> implements IMvpView, IAccountDependencyView, IErrorView, IToastView {

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if(isAdded()){
            Toast.makeText(getActivity(), getString(titleTes), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if(isAdded()){
            Utils.showRedTopToast(getActivity(), text);
        }
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if(isAdded()){
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }
}

package biz.dealnote.messenger.fragment.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.IProgressView;
import biz.dealnote.messenger.mvp.view.IToastView;
import biz.dealnote.messenger.mvp.view.IToolbarView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.compat.AbsMvpFragment;
import biz.dealnote.mvp.core.AbsPresenter;
import biz.dealnote.mvp.core.IMvpView;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public abstract class BaseMvpFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpFragment<P, V> implements IMvpView, IAccountDependencyView, IProgressView, IErrorView, IToastView, IToolbarView {

    public static final String EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar";

    protected boolean hasHideToolbarExtra(){
        return nonNull(getArguments()) && getArguments().getBoolean(EXTRA_HIDE_TOOLBAR);
    }

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if(isAdded()){
            Toast.makeText(requireActivity(), getString(titleTes, params), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    public Bundle requireArguments(){
        return AssertUtils.requireNonNull(getArguments());
    }

    @Override
    public void showError(String text) {
        if(isAdded()){
            Utils.showRedTopToast(requireActivity(), text);
        }
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if(isAdded()){
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActivityUtils.setToolbarSubtitle(this, subtitle);
    }

    @Override
    public void setToolbarTitle(String title) {
        ActivityUtils.setToolbarTitle(this, title);
    }

    @Override
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    protected static void safelySetCheched(CompoundButton button, boolean checked){
        if(nonNull(button)){
            button.setChecked(checked);
        }
    }

    protected static void safelySetText(TextView target, String text){
        if(nonNull(target)){
            target.setText(text);
        }
    }

    protected static void safelySetText(TextView target, @StringRes int text){
        if(nonNull(target)){
            target.setText(text);
        }
    }

    protected static void safelySetVisibleOrGone(View target, boolean visible){
        if(nonNull(target)){
            target.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected void styleSwipeRefreshLayoutWithCurrentTheme(@NonNull SwipeRefreshLayout swipeRefreshLayout){
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), swipeRefreshLayout);
    }

    private ProgressDialog mLoadingProgressDialog;

    @Override
    public void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable) {
        dismissProgressDialog();

        mLoadingProgressDialog = new ProgressDialog(requireActivity());
        mLoadingProgressDialog.setTitle(title);
        mLoadingProgressDialog.setMessage(getString(message));
        mLoadingProgressDialog.setCancelable(cancelable);
        mLoadingProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (nonNull(mLoadingProgressDialog)) {
            if (mLoadingProgressDialog.isShowing()) {
                mLoadingProgressDialog.cancel();
            }
        }
    }
}

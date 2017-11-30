package biz.dealnote.mvp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.ViewHostDelegate;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public abstract class AbsPresenterDialogFragment<P extends IPresenter<V>, V extends IMvpView>
        extends DialogFragment implements ViewHostDelegate.PresenterLifecycleCallback<P,V> {

    private ViewHostDelegate<P, V> mViewHostDelegate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenterDelegate().onCreate(getActivity(), getPresenterViewHost(), getLoaderManager(), savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireViewCreated();
    }

    public void fireViewCreated(){
        getPresenterDelegate().onViewCreated();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenterDelegate().onSaveInstanceState(outState);
    }

    @NonNull
    public ViewHostDelegate<P, V> getPresenterDelegate() {
        if (mViewHostDelegate == null) {
            mViewHostDelegate = new ViewHostDelegate<>(this, tag());
        }

        return mViewHostDelegate;
    }

    @Override
    public void onPause() {
        super.onPause();
        getPresenterDelegate().onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPresenterDelegate().onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getPresenterDelegate().onDestroyView();
    }

    @Override
    public void onDestroy() {
        getPresenterDelegate().onDestroy();
        super.onDestroy();
    }

    protected boolean isViewReady() {
        return getPresenterDelegate().isViewReady();
    }

    protected abstract String tag();

    // Override in case of fragment not implementing IPresenter<View> interface
    @SuppressWarnings("unchecked")
    @NonNull
    protected V getPresenterViewHost() {
        return (V) this;
    }

    @Override
    public void onPresenterDestroyed() {

    }

    @Override
    public void onPresenterReceived(@NonNull P presenter) {

    }

    protected P getPresenter(){
        return getPresenterDelegate().getPresenter();
    }

    protected boolean isPresenterPrepared(){
        return getPresenterDelegate().isPresenterPrepared();
    }
}

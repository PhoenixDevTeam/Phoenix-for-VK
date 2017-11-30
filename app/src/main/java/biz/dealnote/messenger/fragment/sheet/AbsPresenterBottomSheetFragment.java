package biz.dealnote.messenger.fragment.sheet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.ViewHostDelegate;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public abstract class AbsPresenterBottomSheetFragment<P extends IPresenter<V>, V extends IMvpView>
        extends BottomSheetDialogFragment implements ViewHostDelegate.PresenterLifecycleCallback<P,V> {

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
}

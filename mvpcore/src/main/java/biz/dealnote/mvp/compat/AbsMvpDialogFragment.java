package biz.dealnote.mvp.compat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public abstract class AbsMvpDialogFragment<P extends IPresenter<V>, V extends IMvpView>
        extends DialogFragment implements ViewHostDelegate.IFactoryProvider<P,V> {

    private final ViewHostDelegate<P, V> mViewHostDelegate = new ViewHostDelegate<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewHostDelegate.onCreate(getActivity(), getPresenterViewHost(), this, getLoaderManager(), savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireViewCreated();
    }

    public void fireViewCreated(){
        mViewHostDelegate.onViewCreated();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewHostDelegate.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewHostDelegate.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewHostDelegate.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewHostDelegate.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mViewHostDelegate.onDestroy();
        super.onDestroy();
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @SuppressWarnings("unchecked")
    @NonNull
    protected V getPresenterViewHost() {
        return (V) this;
    }

    protected P getPresenter(){
        return mViewHostDelegate.getPresenter();
    }
}
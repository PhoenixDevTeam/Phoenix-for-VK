package biz.dealnote.mvp.compat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.PresenterAction;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public abstract class AbsMvpFragment<P extends IPresenter<V>, V extends IMvpView> extends Fragment implements ViewHostDelegate.IFactoryProvider<P,V> {

    private ViewHostDelegate<P, V> mViewHostDelegate = new ViewHostDelegate<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewHostDelegate.onCreate(requireActivity(), getPresenterViewHost(), this, getLoaderManager(), savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewHostDelegate.onViewCreated();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    private final Object toString = new Object();

    @Override
    public String toString() {
        return toString.toString();
    }

    protected P getPresenter(){
        return mViewHostDelegate.getPresenter();
    }

    protected boolean isPresenterPrepared(){
        return mViewHostDelegate.isPresenterPrepared();
    }

    public void callPresenter(PresenterAction<P, V> action){
        mViewHostDelegate.callPresenter(action);
    }

    public void postPrenseterReceive(PresenterAction<P, V> action){
        mViewHostDelegate.postPrenseterReceive(action);
    }
}
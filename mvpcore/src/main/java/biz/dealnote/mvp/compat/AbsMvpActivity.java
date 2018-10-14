package biz.dealnote.mvp.compat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public abstract class AbsMvpActivity<P extends IPresenter<V>, V extends IMvpView> extends AppCompatActivity implements ViewHostDelegate.IFactoryProvider<P,V> {

    private final ViewHostDelegate<P, V> mViewHostDelegate = new ViewHostDelegate<>();

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewHostDelegate.onCreate(this, getPresenterViewHost(), this, getSupportLoaderManager(), savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mViewHostDelegate.onViewCreated();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewHostDelegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewHostDelegate.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewHostDelegate.onResume();
    }

    @Override
    protected void onDestroy() {
        mViewHostDelegate.onDestroyView();
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
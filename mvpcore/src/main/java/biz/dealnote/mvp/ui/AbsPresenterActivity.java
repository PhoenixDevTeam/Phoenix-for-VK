package biz.dealnote.mvp.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.ViewHostDelegate;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public abstract class AbsPresenterActivity<P extends IPresenter<V>, V extends IMvpView> extends AppCompatActivity implements ViewHostDelegate.PresenterLifecycleCallback<P,V> {

    private ViewHostDelegate<P, V> mViewHostDelegate;

    protected abstract String tag();

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenterDelegate().onCreate(this, getPresenterViewHost(),
                getSupportLoaderManager(), savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getPresenterDelegate().onViewCreated();
    }

    @NonNull
    public ViewHostDelegate<P, V> getPresenterDelegate() {
        if (mViewHostDelegate == null) {
            mViewHostDelegate = new ViewHostDelegate<>(this, tag());
        }

        return mViewHostDelegate;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getPresenterDelegate().onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenterDelegate().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenterDelegate().onResume();
    }

    @Override
    protected void onDestroy() {
        getPresenterDelegate().onDestroyView();
        getPresenterDelegate().onDestroy();
        super.onDestroy();
    }

    protected boolean isActivityDestroyed(){
        return getPresenterDelegate().isDestroyed();
    }

    @Override
    public void onPresenterDestroyed() {

    }

    @Override
    public void onPresenterReceived(@NonNull P presenter) {

    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @SuppressWarnings("unchecked")
    @NonNull
    protected V getPresenterViewHost() {
        return (V) this;
    }

    protected P getPresenter(){
        return getPresenterDelegate().getPresenter();
    }

    protected boolean isPresenterPrepared(){
        return getPresenterDelegate().isPresenterPrepared();
    }
}

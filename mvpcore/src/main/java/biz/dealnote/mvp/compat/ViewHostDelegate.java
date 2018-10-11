package biz.dealnote.mvp.compat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.IPresenterFactory;
import biz.dealnote.mvp.core.PresenterAction;

/**
 * Created by ruslan.kolbasa on 08.09.2016.
 * mvpcore
 */
public class ViewHostDelegate<P extends IPresenter<V>, V extends IMvpView> {

    private static final String SAVE_PRESENTER_STATE = "save-presenter-state";
    private static final int LOADER_ID = 101;

    private Bundle mLastKnownPresenterState;

    private boolean mViewCreated;

    private P mPresenter;
    private WeakReference<V> mViewReference;

    public void onCreate(@NonNull final Context context,
                         @NonNull V viewHost,
                         @NonNull final IFactoryProvider<P, V> factoryProvider,
                         @NonNull LoaderManager loaderManager,
                         @Nullable Bundle savedInstanceState) {
        this.mViewReference = new WeakReference<>(viewHost);

        if (savedInstanceState != null) {
            this.mLastKnownPresenterState = savedInstanceState.getBundle(SAVE_PRESENTER_STATE);
        }

        final Context app = context.getApplicationContext();
        SimplePresenterLoader<P, V> loader = (SimplePresenterLoader<P, V>) loaderManager.initLoader(LOADER_ID, mLastKnownPresenterState, new LoaderManager.LoaderCallbacks<P>() {
            @NonNull
            @Override
            public Loader<P> onCreateLoader(int id, Bundle args) {
                return new SimplePresenterLoader<>(app, factoryProvider.getPresenterFactory(args));
            }

            @Override
            public void onLoadFinished(@NonNull Loader<P> loader, P data) {

            }

            @Override
            public void onLoaderReset(@NonNull Loader<P> loader) {
                mPresenter = null;
            }
        });

        mPresenter = loader.get();
        mPresenter.attachViewHost(mViewReference.get());

        List<PresenterAction<P, V>> copy = new ArrayList<>(onReceivePresenterActions);
        onReceivePresenterActions.clear();
        for(PresenterAction<P, V> action : copy){
            action.call(mPresenter);
        }
    }

    public void onDestroy() {
        mPresenter.detachViewHost();
        mViewReference = new WeakReference<>(null);
    }

    public void onViewCreated() {
        if (mViewCreated) {
            return;
        }

        mViewCreated = true;
        mPresenter.createView(mViewReference.get());
    }

    public void onDestroyView() {
        mViewCreated = false;
        mPresenter.destroyView();
    }

    private List<PresenterAction<P, V>> onReceivePresenterActions = new ArrayList<>();

    public void callPresenter(PresenterAction<P, V> action){
        if(isPresenterPrepared()){
            action.call(getPresenter());
        }
    }

    public void postPrenseterReceive(PresenterAction<P, V> action){
        if(isPresenterPrepared()){
            action.call(getPresenter());
        } else {
            onReceivePresenterActions.add(action);
        }
    }

    public P getPresenter() {
        return mPresenter;
    }

    public void onResume() {
        mPresenter.resumeView();
    }

    public void onPause() {
        mPresenter.pauseView();
    }

    public boolean isPresenterPrepared() {
        return mPresenter != null;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mPresenter != null) {
            mLastKnownPresenterState = new Bundle();
            mPresenter.saveState(outState);
        }

        outState.putBundle(SAVE_PRESENTER_STATE, mLastKnownPresenterState);
    }

    public interface IFactoryProvider<P extends IPresenter<V>, V extends IMvpView> {
        IPresenterFactory<P> getPresenterFactory(@Nullable Bundle saveInstanceState);
    }
}
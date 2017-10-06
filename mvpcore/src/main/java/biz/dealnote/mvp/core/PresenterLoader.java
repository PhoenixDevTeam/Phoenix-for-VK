package biz.dealnote.mvp.core;

import android.content.Context;
import android.support.v4.content.Loader;

import biz.dealnote.mvp.Logger;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public final class PresenterLoader<T extends IPresenter> extends Loader<T> {

    private final IPresenterFactory<T> factory;
    private T presenter;
    private final String tag;

    private OnLoadCompleteListener<T> mAdditionaLoadCompleteListener;

    public PresenterLoader(Context context, IPresenterFactory<T> factory, String tag) {
        super(context);
        this.factory = factory;
        this.tag = tag;
    }

    public void setAdditionaLoadCompleteListener(OnLoadCompleteListener<T> additionaLoadCompleteListener) {
        this.mAdditionaLoadCompleteListener = additionaLoadCompleteListener;
    }

    @Override
    protected void onStartLoading() {
        Logger.i("loader", "onStartLoading-" + tag);

        // if we already own a presenter instance, simply deliver it.
        if (presenter != null) {
            deliverResult(presenter);
            return;
        }

        // Otherwise, force a load
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        Logger.i("loader", "onForceLoad-" + tag);

        // Create the IPresenter using the Factory
        presenter = factory.create();

        // Deliver the result
        deliverResult(presenter);
    }

    @Override
    public void deliverResult(T data) {
        super.deliverResult(data);
        if(mAdditionaLoadCompleteListener != null){
            mAdditionaLoadCompleteListener.onLoadComplete(this, data);
        }

        Logger.i("loader", "deliverResult-" + tag);
    }

    @Override
    protected void onStopLoading() {
        Logger.i("loader", "onStopLoading-" + tag);
    }

    @Override
    protected void onReset() {
        Logger.i("loader", "onReset-" + tag + ", mAdditionaLoadCompleteListener: " + mAdditionaLoadCompleteListener);
        if (presenter != null) {
            presenter.onDestroyed();
            presenter = null;
        }
    }
}
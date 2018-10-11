package biz.dealnote.mvp.compat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import biz.dealnote.mvp.core.IMvpView;
import biz.dealnote.mvp.core.IPresenter;
import biz.dealnote.mvp.core.IPresenterFactory;

public class SimplePresenterLoader<P extends IPresenter<V>, V extends IMvpView> extends Loader<P> {

    private IPresenterFactory<P> factory;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    SimplePresenterLoader(@NonNull Context context, @NonNull IPresenterFactory<P> factory) {
        super(context);
        this.factory = factory;
    }

    private P presenter;

    @NonNull
    public P get(){
        if(presenter == null){
            presenter = factory.create();
            factory = null;
        }

        return presenter;
    }

    @Override
    protected void onReset() {
        super.onReset();
        if (presenter != null) {
            presenter.destroy();
            presenter = null;
        }
    }
}
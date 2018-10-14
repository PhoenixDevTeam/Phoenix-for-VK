package biz.dealnote.mvp.compat

import android.content.Context
import android.support.v4.content.Loader

import biz.dealnote.mvp.core.IMvpView
import biz.dealnote.mvp.core.IPresenter
import biz.dealnote.mvp.core.IPresenterFactory

class SimplePresenterLoader<P : IPresenter<V>, V : IMvpView> constructor(context: Context, var factory: IPresenterFactory<P>) : Loader<P>(context) {

    private var f: IPresenterFactory<P>? = factory

    private var presenter: P? = null

    fun get(): P {
        if (presenter == null) {
            presenter = factory.create()
            f = null
        }

        return presenter!!
    }

    override fun onReset() {
        super.onReset()
        presenter?.destroy()
        presenter = null
    }
}
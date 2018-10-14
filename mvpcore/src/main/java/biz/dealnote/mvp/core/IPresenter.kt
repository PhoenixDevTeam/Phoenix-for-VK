package biz.dealnote.mvp.core

import android.os.Bundle

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
interface IPresenter<V : IMvpView> {
    fun saveState(outState: Bundle)

    fun destroy()
    fun resumeView()
    fun pauseView()
    fun attachViewHost(view: V)
    fun detachViewHost()
    fun createView(view: V)
    fun destroyView()
}
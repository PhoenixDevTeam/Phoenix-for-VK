package biz.dealnote.mvp.core

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
interface PresenterAction<P : IPresenter<V>, V : IMvpView> {
    fun call(presenter: P)
}
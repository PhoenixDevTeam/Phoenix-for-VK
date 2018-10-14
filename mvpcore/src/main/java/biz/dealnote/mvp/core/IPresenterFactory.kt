package biz.dealnote.mvp.core

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
interface IPresenterFactory<T : IPresenter<*>> {
    fun create(): T
}
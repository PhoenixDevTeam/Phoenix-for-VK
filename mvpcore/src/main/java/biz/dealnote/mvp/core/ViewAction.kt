package biz.dealnote.mvp.core

/**
 * Created by admin on 26.03.2017.
 * phoenix
 */
interface ViewAction<V> {
    fun call(view: V)
}
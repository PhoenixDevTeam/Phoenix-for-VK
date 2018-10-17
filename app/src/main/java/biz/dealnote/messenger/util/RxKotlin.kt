package biz.dealnote.messenger.util

import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.functions.BiFunction

object RxKotlin {
    inline fun <T, U, R> zip(s1: SingleSource<T>, s2: SingleSource<U>, crossinline zipper: (T, U) -> R): Single<R> = Single.zip(s1, s2, BiFunction { t, u -> zipper.invoke(t, u) })
}
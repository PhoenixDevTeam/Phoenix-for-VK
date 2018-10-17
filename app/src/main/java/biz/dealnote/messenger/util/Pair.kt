package biz.dealnote.messenger.util

/**
 * Created by ruslan.kolbasa on 01.02.2017.
 * phoenix
 */
class Pair<F, S>(val first: F, val second: S) {

    companion object {

        fun <F, S> create(first: F, second: S): Pair<F, S> {
            return Pair(first, second)
        }
    }
}

package biz.dealnote.mvp.reflect

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
class EventHandler(private val target: Any, private val method: Method) {

    private val hashCode: Int

    init {
        method.isAccessible = true

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        val prime = 31
        hashCode = (prime + method.hashCode()) * prime + target.hashCode()
    }

    @Throws(InvocationTargetException::class)
    fun handle() {
        try {
            method.invoke(target)
        } catch (e: IllegalAccessException) {
            throw AssertionError(e)
        } catch (e: InvocationTargetException) {
            if (e.cause is Error) {
                throw e.cause as Error
            }
            throw e
        }
    }

    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null) {
            return false
        }

        if (javaClass != other.javaClass) {
            return false
        }

        val o = other as EventHandler
        return method == o.method && target === o.target
    }
}
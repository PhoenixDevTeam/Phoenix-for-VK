package biz.dealnote.mvp.reflect

import java.lang.reflect.Method
import java.util.*

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
object AnnotatedHandlerFinder {

    /**
     * Cache @OnGuiCreated methods for each class.
     */
    private val cache = HashMap<Class<*>, MutableSet<Method>>()

    private fun loadAnnotatedSubscriberMethods(listenerClass: Class<*>, methods: MutableSet<Method>, includeSuperclass: Class<*>) {
        loadAnnotatedMethods(listenerClass, methods)

        if (listenerClass != includeSuperclass) {
            loadAnnotatedSubscriberMethods(listenerClass.superclass, methods, includeSuperclass)
        }
    }

    /**
     * Load all methods annotated with [OnGuiCreated] into their respective caches for the
     * specified class.
     */
    private fun loadAnnotatedMethods(listenerClass: Class<*>, methods: MutableSet<Method>) {
        for (method in listenerClass.declaredMethods) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored for
            // subscribe/produce.
            if (method.isBridge) {
                continue
            }

            if (method.isAnnotationPresent(OnGuiCreated::class.java)) {
                val parameterTypes = method.parameterTypes

                if (parameterTypes.isNotEmpty()) {
                    throw IllegalArgumentException("Method $method has @OnGuiCreated annotation " +
                            "but requires ${parameterTypes.size} arguments.  Methods must require zero arguments.")
                }

                methods.add(method)
            }
        }

        cache[listenerClass] = methods
    }

    /**
     * This implementation finds all methods marked with a [OnGuiCreated] annotation.
     */
    fun findAllOnGuiCreatedHandlers(listener: Any, includeSuperclass: Class<*>): Set<EventHandler> {
        val listenerClass = listener::class.java

        var methods: MutableSet<Method>? = cache[listenerClass]
        if (null == methods) {
            methods = HashSet()
            loadAnnotatedSubscriberMethods(listenerClass, methods, includeSuperclass)
        }

        val handlers = HashSet<EventHandler>(methods.size)

        for (m in methods) {
            handlers.add(EventHandler(listener, m))
        }

        return handlers
    }
}
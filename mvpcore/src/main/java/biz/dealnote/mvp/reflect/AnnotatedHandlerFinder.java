package biz.dealnote.mvp.reflect;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public final class AnnotatedHandlerFinder {

    /**
     * Cache @OnGuiCreated methods for each class.
     */
    private static final ConcurrentMap<Class<?>, Set<Method>> METHODS_ON_GUI_CREATED_CACHE = new ConcurrentHashMap<>();

    private static void loadAnnotatedSubscriberMethods(Class<?> listenerClass, Set<Method> methods,
                                                       Class<?> includeSuperclass) {
        loadAnnotatedMethods(listenerClass, methods);

        if(listenerClass.equals(includeSuperclass)) return;

        Class<?> superclass = listenerClass.getSuperclass();
        loadAnnotatedSubscriberMethods(superclass, methods, includeSuperclass);
    }

    /**
     * Load all methods annotated with {@link OnGuiCreated} into their respective caches for the
     * specified class.
     */
    private static void loadAnnotatedMethods(Class<?> listenerClass, Set<Method> methods) {
        for (Method method : listenerClass.getDeclaredMethods()) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored for
            // subscribe/produce.
            if (method.isBridge()) {
                continue;
            }

            if (method.isAnnotationPresent(OnGuiCreated.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 0) {
                    throw new IllegalArgumentException("Method " + method + " has @OnGuiCreated annotation but requires "
                            + parameterTypes.length + " arguments.  Methods must require zero arguments.");
                }

                //if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                //    throw new IllegalArgumentException("Method " + method + " has @OnGuiCreated annotation but is not 'public'.");
                //}

                methods.add(method);
            }
        }

        METHODS_ON_GUI_CREATED_CACHE.put(listenerClass, methods);
    }

    /**
     * This implementation finds all methods marked with a {@link OnGuiCreated} annotation.
     */
    @NonNull
    public static Set<EventHandler> findAllOnGuiCreatedHandlers(Object listener, Class<?> includeSuperclass) {
        Class<?> listenerClass = listener.getClass();
        Set<EventHandler> handlersInMethod = new HashSet<>();

        Set<Method> methods = METHODS_ON_GUI_CREATED_CACHE.get(listenerClass);
        if (null == methods) {
            methods = new HashSet<>();
            loadAnnotatedSubscriberMethods(listenerClass, methods, includeSuperclass);
        }

        if (!methods.isEmpty()) {
            for (Method m : methods) {
                handlersInMethod.add(new EventHandler(listener, m));
            }
        }

        return handlersInMethod;
    }

    private AnnotatedHandlerFinder() {
        // No instances.
    }
}
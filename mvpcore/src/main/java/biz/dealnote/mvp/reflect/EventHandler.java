package biz.dealnote.mvp.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public class EventHandler {

    /**
     * Object sporting the handler method.
     */
    private final Object target;
    /**
     * Handler method.
     */
    private final Method method;
    /**
     * Object hash code.
     */
    private final int hashCode;
    /**
     * Should this handler receive events?
     */
    private boolean valid = true;

    public EventHandler(Object target, Method method) {
        if (target == null) {
            throw new NullPointerException("EventHandler target cannot be null.");
        }

        if (method == null) {
            throw new NullPointerException("EventHandler method cannot be null.");
        }

        this.target = target;
        this.method = method;

        method.setAccessible(true);

        // Compute hash code eagerly since we know it will be used frequently and we cannot estimate the runtime of the
        // target's hashCode call.
        final int prime = 31;
        hashCode = (prime + method.hashCode()) * prime + target.hashCode();
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * If invalidated, will subsequently refuse to handle events.
     * <p>
     * Should be called when the wrapped object is unregistered from the Bus.
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * Invokes the wrapped handler method to handle {@code event}.
     *
     * @throws IllegalStateException             if previously invalidated.
     * @throws InvocationTargetException if the wrapped method throws any {@link Throwable} that is not
     *                                                     an {@link Error} ({@code Error}s are propagated as-is).
     */
    public void handle() throws InvocationTargetException {
        if (!valid) {
            throw new IllegalStateException(toString() + " has been invalidated and can no longer handle events.");
        }

        try {
            method.invoke(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public String toString() {
        return "[EventHandler " + method + "]";
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final EventHandler other = (EventHandler) obj;
        return method.equals(other.method) && target == other.target;
    }
}

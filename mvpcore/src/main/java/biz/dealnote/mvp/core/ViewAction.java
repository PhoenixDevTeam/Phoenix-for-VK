package biz.dealnote.mvp.core;

/**
 * Created by admin on 26.03.2017.
 * phoenix
 */
public interface ViewAction<V> {
    void call(V view);
}

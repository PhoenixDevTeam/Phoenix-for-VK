package biz.dealnote.messenger.util;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class WeakActionHandler<T> extends Handler {

    private final WeakReference<T> ref;

    public WeakActionHandler(T object){
        this.ref = new WeakReference<>(object);
    }

    private Action<T> action;

    @Override
    public final void handleMessage(Message msg) {
        T object = ref.get();
        if(Objects.nonNull(action)){
            action.doAction(msg.what, object);
        }
    }

    public WeakActionHandler setAction(Action<T> action) {
        this.action = action;
        return this;
    }

    public interface Action<T> {
        void doAction(int what, T object);
    }
}

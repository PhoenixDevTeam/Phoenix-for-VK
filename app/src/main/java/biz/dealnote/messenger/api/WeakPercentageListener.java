package biz.dealnote.messenger.api;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.util.Objects;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public class WeakPercentageListener implements PercentageListener {

    private final WeakReference<PercentageListener> ref;

    public WeakPercentageListener(PercentageListener listener) {
        this.ref = new WeakReference<>(listener);
    }

    @Override
    public void onProgressChanged(int percentage) {
        PercentageListener orig = ref.get();
        if(Objects.nonNull(orig)){
            orig.onProgressChanged(percentage);
        }
    }
}
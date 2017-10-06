package biz.dealnote.messenger.view.pager;

import com.squareup.picasso.Callback;

import java.lang.ref.WeakReference;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
public class WeakPicassoLoadCallback implements Callback {

    private WeakReference<Callback> mReference;

    public WeakPicassoLoadCallback(Callback baseCallback) {
        this.mReference = new WeakReference<>(baseCallback);
    }

    @Override
    public void onSuccess() {
        Callback callback = mReference.get();
        if (callback != null) {
            callback.onSuccess();
        }
    }

    @Override
    public void onError(Exception e) {
        Callback callback = mReference.get();
        if (callback != null) {
            callback.onError(e);
        }
    }

}

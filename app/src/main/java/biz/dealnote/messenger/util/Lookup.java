package biz.dealnote.messenger.util;

import android.os.Handler;

/**
 * Created by ruslan.kolbasa on 04.10.2016.
 * phoenix
 */
public class Lookup {

    private static final int LOOKUP = 1540;

    private Handler mHandler;

    private int mDelay;

    public Lookup(int initialDelay) {
        mDelay = initialDelay;
        mHandler = new Handler(msg -> {
            onLookupHandle();
            return true;
        });
    }

    private void onLookupHandle(){
        mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        if(Objects.nonNull(mCallback)){
            mCallback.onIterated();
        }
    }

    public void changeDelayTime(int delay, boolean startNow){
        mDelay = delay;
        if(startNow){
            mHandler.removeMessages(LOOKUP);
            mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        }
    }

    public void stop(){
        mHandler.removeMessages(LOOKUP);
    }

    public void start(){
        if(!mHandler.hasMessages(LOOKUP)){
            mHandler.sendEmptyMessageDelayed(LOOKUP, mDelay);
        }
    }

    private Callback mCallback;

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public interface Callback {
        void onIterated();
    }
}
package biz.dealnote.messenger.util;

import android.text.TextUtils;
import android.util.Log;

import biz.dealnote.messenger.BuildConfig;

/**
 * Created by hp-dv6 on 05.06.2016.
 * VKMessenger
 */
public class Exestime {

    private static final String TAG = Exestime.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void log(String method, long startTime, Object... params) {
        if(!DEBUG) return;

        if (params == null || params.length == 0) {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms");
        } else {
            Log.d(TAG, method + ", time: " + (System.currentTimeMillis() - startTime) + " ms, params: [" + TextUtils.join(", ", params) + "]");
        }
    }
}

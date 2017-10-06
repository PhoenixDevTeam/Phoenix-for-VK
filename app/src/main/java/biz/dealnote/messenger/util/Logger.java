package biz.dealnote.messenger.util;

import android.util.Log;

import biz.dealnote.messenger.BuildConfig;

public class Logger {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static void i(String tag, String messsage){
        if(DEBUG){
            Log.i(tag, messsage);
        }
    }

    public static void d(String tag, String messsage){
        if(DEBUG){
            Log.d(tag, messsage);
        }
    }

    public static void e(String tag, String messsage){
        if(DEBUG){
            Log.e(tag, messsage);
        }
    }

    public static void wtf(String tag, String messsage) {
        if(DEBUG){
            Log.wtf(tag, messsage);
        }
    }
}

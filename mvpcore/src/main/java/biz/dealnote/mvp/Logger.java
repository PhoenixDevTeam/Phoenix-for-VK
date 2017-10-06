package biz.dealnote.mvp;

import android.util.Log;

/**
 * Created by admin on 06.07.2016.
 * mvpcore
 */
public class Logger {

    //private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG = true;

    public static void wtf(String tag, String messsage){
        if(DEBUG){
            Log.wtf(tag, messsage);
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

    public static void i(String tag, String messsage){
        if(DEBUG){
            Log.i(tag, messsage);
        }
    }
}

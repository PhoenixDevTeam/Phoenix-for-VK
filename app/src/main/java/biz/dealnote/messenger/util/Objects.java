package biz.dealnote.messenger.util;

import android.support.annotation.Nullable;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class Objects {

    public static boolean nonNull(@Nullable Object o){
        return o != null;
    }

    public static boolean isNull(@Nullable Object o){
        return o == null;
    }
}

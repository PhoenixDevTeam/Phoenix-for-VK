package biz.dealnote.messenger.crypt;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({KeyLocationPolicy.PERSIST, KeyLocationPolicy.RAM})
@Retention(RetentionPolicy.SOURCE)
public @interface KeyLocationPolicy {

    int PERSIST = 1;

    /**
     * Not yet implemented
     */
    int RAM = 2;
}

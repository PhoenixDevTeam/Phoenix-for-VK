package biz.dealnote.messenger.settings;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({NightMode.DISABLE, NightMode.ENABLE, NightMode.AUTO})
@Retention(RetentionPolicy.SOURCE)
public @interface NightMode {
    int DISABLE = 1;
    int ENABLE = 2;
    int AUTO = 3;
}

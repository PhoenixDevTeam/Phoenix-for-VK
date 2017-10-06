package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({Sex.MAN, Sex.WOMAN, Sex.UNKNOWN})
@Retention(RetentionPolicy.SOURCE)
public @interface Sex {
    int MAN = 1;
    int WOMAN = 2;
    int UNKNOWN = 0;
}

package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({OwnerType.USER, OwnerType.COMMUNITY})
@Retention(RetentionPolicy.SOURCE)
public @interface OwnerType {
    int USER = 1;
    int COMMUNITY = 2;
}

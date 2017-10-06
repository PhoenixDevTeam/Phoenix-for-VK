package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({InternalVideoSize.SIZE_240, InternalVideoSize.SIZE_360, InternalVideoSize.SIZE_480, InternalVideoSize.SIZE_720, InternalVideoSize.SIZE_1080})
@Retention(RetentionPolicy.SOURCE)
public @interface InternalVideoSize {
    int SIZE_240 = 240;
    int SIZE_360 = 360;
    int SIZE_480 = 480;
    int SIZE_720 = 720;
    int SIZE_1080 = 1080;
}

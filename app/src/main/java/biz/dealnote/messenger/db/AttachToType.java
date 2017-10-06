package biz.dealnote.messenger.db;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({AttachToType.MESSAGE, AttachToType.COMMENT, AttachToType.POST})
@Retention(RetentionPolicy.SOURCE)
public @interface AttachToType {
    int MESSAGE = 1;
    int COMMENT = 2;
    int POST = 3;
}

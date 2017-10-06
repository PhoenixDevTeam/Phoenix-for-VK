package biz.dealnote.messenger.settings;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({AvatarStyle.CIRCLE, AvatarStyle.OVAL})
@Retention(RetentionPolicy.SOURCE)
public @interface AvatarStyle {
    int CIRCLE = 1;
    int OVAL = 2;
}

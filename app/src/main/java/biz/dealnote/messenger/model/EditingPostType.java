package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({EditingPostType.DRAFT, EditingPostType.TEMP})
@Retention(RetentionPolicy.SOURCE)
public @interface EditingPostType {
    int DRAFT = 2;
    int TEMP = 3;
}

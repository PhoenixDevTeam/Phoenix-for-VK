package biz.dealnote.messenger.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({CommentedType.POST, CommentedType.PHOTO, CommentedType.VIDEO, CommentedType.TOPIC})
@Retention(RetentionPolicy.SOURCE)
public @interface CommentedType {
    int POST = 1;
    int PHOTO = 2;
    int VIDEO = 3;
    int TOPIC = 4;
}

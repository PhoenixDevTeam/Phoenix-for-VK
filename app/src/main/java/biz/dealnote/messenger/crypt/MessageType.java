package biz.dealnote.messenger.crypt;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({MessageType.KEY_EXCHANGE, MessageType.CRYPTED, MessageType.NORMAL})
@Retention(RetentionPolicy.SOURCE)
public @interface MessageType {
    int KEY_EXCHANGE = 1;
    int CRYPTED = 2;
    int NORMAL = 0;
}

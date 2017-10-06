package biz.dealnote.messenger.longpoll.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({RealtimeAction.MESSAGES_FLAGS_CHANGE, RealtimeAction.MESSAGES_FLAGS_SET, RealtimeAction.MESSAGES_FLAGS_RESET,
        RealtimeAction.MESSAGES_READ, RealtimeAction.USER_IS_ONLINE,
        RealtimeAction.USER_IS_OFFLINE, RealtimeAction.CHAT_PARAMS_WAS_CHANGED, RealtimeAction.USER_WRITE_TEXT,
        RealtimeAction.USER_CALL, RealtimeAction.COUNTER_UNREAD_WAS_CHANGED, RealtimeAction.KEEP_LISTENING_REQUEST})
@Retention(RetentionPolicy.SOURCE)
public @interface RealtimeAction {
    int MESSAGES_FLAGS_CHANGE = 1;
    int MESSAGES_FLAGS_SET = 2;
    int MESSAGES_FLAGS_RESET = 3;
    int MESSAGES_READ = 6;
    int USER_IS_ONLINE = 8;
    int USER_IS_OFFLINE = 9;
    int CHAT_PARAMS_WAS_CHANGED = 51;
    int USER_WRITE_TEXT = 61;
    int USER_CALL = 70;
    int COUNTER_UNREAD_WAS_CHANGED = 80;
    int KEEP_LISTENING_REQUEST = 500;
}

package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({ChatAction.NO_ACTION, ChatAction.PHOTO_UPDATE, ChatAction.PHOTO_REMOVE, ChatAction.CREATE,
        ChatAction.TITLE_UPDATE, ChatAction.INVITE_USER, ChatAction.KICK_USER})
@Retention(RetentionPolicy.SOURCE)
public @interface ChatAction {
    int NO_ACTION = 0;
    int PHOTO_UPDATE = 1;
    int PHOTO_REMOVE = 2;
    int CREATE = 3;
    int TITLE_UPDATE = 4;
    int INVITE_USER = 5;
    int KICK_USER = 6;
}

package biz.dealnote.messenger.api.model.longpoll;

public class AbsLongpollEvent {

    public static final int ACTION_MESSAGES_FLAGS_CHANGE = 1;
    public static final int ACTION_MESSAGES_FLAGS_SET = 2;
    public static final int ACTION_MESSAGES_FLAGS_RESET = 3;
    public static final int ACTION_MESSAGE_ADDED = 4;
    public static final int ACTION_SET_INPUT_MESSAGES_AS_READ = 6;
    public static final int ACTION_SET_OUTPUT_MESSAGES_AS_READ = 7;
    public static final int ACTION_USER_IS_ONLINE = 8;
    public static final int ACTION_USER_IS_OFFLINE = 9;
    public static final int ACTION_CHAT_PARAMS_WAS_CHANGED = 51;
    public static final int ACTION_USER_WRITE_TEXT_IN_DIALOG = 61;
    public static final int ACTION_USER_WRITE_TEXT_IN_CHAT = 62;
    public static final int ACTION_USER_CALL = 70;
    public static final int ACTION_COUNTER_UNREAD_WAS_CHANGED = 80;

    public final int action;

    public int getAction() {
        return action;
    }

    public AbsLongpollEvent(int action) {
        this.action = action;
    }
}

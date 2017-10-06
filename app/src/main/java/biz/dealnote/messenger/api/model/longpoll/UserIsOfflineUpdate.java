package biz.dealnote.messenger.api.model.longpoll;

public class UserIsOfflineUpdate extends AbsLongpollEvent {

    public UserIsOfflineUpdate() {
        super(ACTION_USER_IS_OFFLINE);
    }

    public int user_id;
    public int flags;

    public int getUserId() {
        return user_id;
    }

    public int getFlags() {
        return flags;
    }
}
package biz.dealnote.messenger.api.model.longpoll;

public class UserIsOnlineUpdate extends AbsLongpollEvent {

    public UserIsOnlineUpdate() {
        super(ACTION_USER_IS_ONLINE);
    }

    public int user_id;
    public int extra;

    public int getUserId() {
        return user_id;
    }
}
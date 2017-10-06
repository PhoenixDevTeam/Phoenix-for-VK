package biz.dealnote.messenger.api.model.longpoll;

public class WriteTextInDialogUpdate extends AbsLongpollEvent {

    public WriteTextInDialogUpdate() {
        super(ACTION_USER_WRITE_TEXT_IN_DIALOG);
    }

    public int user_id;
    public int flags;

    public int getUserId() {
        return user_id;
    }
}
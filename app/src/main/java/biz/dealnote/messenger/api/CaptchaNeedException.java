package biz.dealnote.messenger.api;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class CaptchaNeedException extends Exception {

    private final String sid;

    private final String img;

    public CaptchaNeedException(String sid, String img) {
        this.sid = sid;
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public String getSid() {
        return sid;
    }
}
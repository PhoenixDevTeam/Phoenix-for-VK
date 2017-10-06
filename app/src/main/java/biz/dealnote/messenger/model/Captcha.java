package biz.dealnote.messenger.model;

/**
 * Created by Ruslan Kolbasa on 19.07.2017.
 * phoenix
 */
public class Captcha {

    private final String sid;

    private final String img;

    public Captcha(String sid, String img) {
        this.sid = sid;
        this.img = img;
    }

    public String getSid() {
        return sid;
    }

    public String getImg() {
        return img;
    }
}
package biz.dealnote.messenger.api.model;

/**
 * Created by Ruslan Kolbasa on 06.06.2017.
 * phoenix
 */
public class Captcha {

    private final String sid;

    private final String img;

    public Captcha(String sid, String img) {
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

package biz.dealnote.messenger.exception;

import android.os.Bundle;

import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 27.10.2016.
 * phoenix
 */
public class ApiServiceException extends ServiceException {

    private int code;

    private Captcha captcha;

    ApiServiceException(String message, Bundle bundle) {
        this(message, bundle.getInt("code"));

        String captchaSid = bundle.getString("captcha_sid");
        String captchaImg = bundle.getString("captcha_img");
        this.captcha = Objects.nonNull(captchaImg) && Objects.nonNull(captchaSid)
                ? new Captcha(captchaSid, captchaImg) : null;
    }

    public ApiServiceException(String message, int code, Captcha captcha) {
        super(message, Type.API);
        this.captcha = captcha;
        this.code = code;
    }

    public ApiServiceException(String message, int code) {
        this(message, code, null);
    }

    @Override
    public Bundle serializeToBundle() {
        Bundle bundle = super.serializeToBundle();
        bundle.putInt("code", code);
        bundle.putString("captcha_sid", Objects.isNull(captcha) ? null : captcha.getSid());
        bundle.putString("captcha_img", Objects.isNull(captcha) ? null : captcha.getImg());
        return bundle;
    }

    public int getCode() {
        return code;
    }

    public Captcha getCaptcha() {
        return captcha;
    }

    public static final class Captcha {

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
}

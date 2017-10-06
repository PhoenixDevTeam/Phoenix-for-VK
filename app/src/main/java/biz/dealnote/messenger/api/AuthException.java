package biz.dealnote.messenger.api;

/**
 * Created by Ruslan Kolbasa on 01.08.2017.
 * phoenix
 */
public class AuthException extends Exception {

    private final String code;

    public AuthException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        String desc = super.getMessage();
        if (desc != null && desc.length() > 0) {
            return desc;
        }

        return "Unexpected auth error, code: [" + code + "]";
    }
}
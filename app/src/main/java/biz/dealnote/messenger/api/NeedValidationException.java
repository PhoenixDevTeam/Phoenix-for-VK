package biz.dealnote.messenger.api;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class NeedValidationException extends Exception {

    private final String type;

    public NeedValidationException(String type) {
        this.type = type;
    }

    public String getValidationType() {
        return type;
    }
}
package biz.dealnote.messenger.crypt;

/**
 * Created by ruslan.kolbasa on 20.10.2016.
 * phoenix
 */
public class InvalidSessionStateException extends Exception {

    public InvalidSessionStateException() {
    }

    public InvalidSessionStateException(String message) {
        super(message);
    }
}

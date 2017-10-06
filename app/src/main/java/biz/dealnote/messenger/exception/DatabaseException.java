package biz.dealnote.messenger.exception;

/**
 * Created by ruslan.kolbasa on 13.10.2016.
 * phoenix
 */
public class DatabaseException extends Exception {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException() {
    }
}

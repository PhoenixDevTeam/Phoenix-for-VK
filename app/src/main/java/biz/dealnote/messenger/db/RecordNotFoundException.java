package biz.dealnote.messenger.db;

/**
 * Created by Ruslan Kolbasa on 12.05.2017.
 * phoenix
 */
public class RecordNotFoundException extends Exception {

    public RecordNotFoundException(String message) {
        super(message);
    }
}

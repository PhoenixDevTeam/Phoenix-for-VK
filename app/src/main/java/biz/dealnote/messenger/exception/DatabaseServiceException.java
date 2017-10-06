package biz.dealnote.messenger.exception;

/**
 * Created by ruslan.kolbasa on 27.10.2016.
 * phoenix
 */
public class DatabaseServiceException extends ServiceException {

    public DatabaseServiceException(String message) {
        super(message, Type.DB);
    }
}

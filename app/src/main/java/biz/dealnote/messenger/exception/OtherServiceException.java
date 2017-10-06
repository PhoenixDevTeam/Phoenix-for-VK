package biz.dealnote.messenger.exception;

/**
 * Created by ruslan.kolbasa on 27.10.2016.
 * phoenix
 */
public class OtherServiceException extends ServiceException {

    public OtherServiceException(String message) {
        super(message, Type.OTHER);
    }
}

package biz.dealnote.messenger.exception;

/**
 * Created by ruslan.kolbasa on 01.02.2017.
 * phoenix
 */
public class NotFoundException extends Exception {

    public NotFoundException(){

    }

    public NotFoundException(String message){
        super(message);
    }
}

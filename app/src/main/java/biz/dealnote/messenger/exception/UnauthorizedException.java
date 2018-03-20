package biz.dealnote.messenger.exception;

import java.io.IOException;

/**
 * Created by admin on 3/20/2018.
 * Phoenix-for-VK
 */
public class UnauthorizedException extends IOException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
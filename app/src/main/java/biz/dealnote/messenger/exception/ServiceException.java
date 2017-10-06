package biz.dealnote.messenger.exception;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.foxykeep.datadroid.exception.CustomRequestException;

/**
 * Created by ruslan.kolbasa on 12.10.2016.
 * phoenix
 */
public abstract class ServiceException extends CustomRequestException {

    private int type;

    public ServiceException(String message, int type) {
        super(message);
        this.type = type;
    }

    public Bundle serializeToBundle(){
        Bundle bundle = new Bundle();
        bundle.putString("message", getMessage());
        bundle.putInt("type", type);
        return bundle;
    }

    public static ServiceException deserializeFromBundle(@NonNull Bundle bundle){
        String message = bundle.getString("message");
        int type = bundle.getInt("type");

        switch (type){
            case Type.API:
                return new ApiServiceException(message, bundle);
            case Type.OTHER:
                return new OtherServiceException(message);
            case Type.DB:
                return new DatabaseServiceException(message);
            default:
                throw new IllegalArgumentException("Invalid exception type");
        }
    }

    public int getType() {
        return type;
    }

    public static final class Type {
        public static final int API = 1;
        public static final int DB = 2;
        public static final int OTHER = 3;
    }
}

package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;

public class PhotoRequestFactory {

    public static final int REQUEST_CREATE_ALBUM = 11001;
    public static final int REQUEST_EDIT_ALBUM = 11002;
    public static final int REQUEST_DELETE = 11018;
    public static final int REQUEST_RESTORE = 11019;

    public static Request getRestoreRequest(int photoId, int ownerId){
        Request request = new Request(REQUEST_RESTORE);
        request.put(Extra.PHOTO_ID, photoId);
        request.put(Extra.OWNER_ID, ownerId);
        return request;
    }

    public static Request getDeleteRequest(int photoId, int ownerId){
        Request request = new Request(REQUEST_DELETE);
        request.put(Extra.PHOTO_ID, photoId);
        request.put(Extra.OWNER_ID, ownerId);
        return request;
    }
}

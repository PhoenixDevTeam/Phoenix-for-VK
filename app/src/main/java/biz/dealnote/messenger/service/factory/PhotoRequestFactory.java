package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.util.Objects;

public class PhotoRequestFactory {

    public static final int REQUEST_CREATE_ALBUM = 11001;
    public static final int REQUEST_EDIT_ALBUM = 11002;
    public static final int REQUEST_SEARCH = 11007;
    public static final int REQUEST_COPY = 11009;
    public static final int REQUEST_DELETE_ALBUM = 11017;
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

    public static Request getDeleteAlbumRequest(int albumId, Integer groupId){
        Request request = new Request(REQUEST_DELETE_ALBUM);
        request.put(Extra.ALBUM_ID, albumId);
        if(Objects.nonNull(groupId)){
            request.put(Extra.GROUP_ID, groupId);
        }
        return request;
    }

    public static Request getCopyRequest(int ownerId, int photoId, String accessKey) {
        Request request = new Request(REQUEST_COPY);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.PHOTO_ID, photoId);
        request.put(Extra.ACCESS_KEY, accessKey);
        return request;
    }
}

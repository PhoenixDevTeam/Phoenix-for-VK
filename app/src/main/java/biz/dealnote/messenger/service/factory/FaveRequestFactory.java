package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_LINK_ID;

public class FaveRequestFactory {

    public static final int REQUEST_GET_LINKS = 8005;
    public static final int REQUEST_REMOVE_USER = 8007;
    public static final int REQUEST_REMOVE_LINK = 8011;

    public static Request getRemoveLinkRequest(String linkId){
        Request request = new Request(REQUEST_REMOVE_LINK);
        request.put(EXTRA_LINK_ID, linkId);
        return request;
    }

    public static Request getRemoveUserRequest(int userId){
        Request request = new Request(REQUEST_REMOVE_USER);
        request.put(Extra.USER_ID, userId);
        return request;
    }

    public static Request getGetLinksRequest(int offset, int count){
        Request request = new Request(REQUEST_GET_LINKS);
        request.put(Extra.OFFSET, offset);
        request.put(Extra.COUNT, count);
        return request;
    }
}
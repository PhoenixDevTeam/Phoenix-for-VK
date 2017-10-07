package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.service.IntArray;

public class AudioRequestFactory {

    public static final int REQUEST_BROADCAST = 14003;
    public static final int REQUEST_FIND_COVER = 14005;

    public static Request getFindCoverRequest(int audioId, int ownerId, String artist, String title){
        Request request = new Request(REQUEST_FIND_COVER);
        request.put(Extra.ID, audioId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.ARTIST, artist);
        request.put(Extra.TITLE, title);
        return request;
    }

    public static Request getBroadcastRequest(int audioId, int ownerId, IntArray targetIds){
        Request request = new Request(REQUEST_BROADCAST);
        request.put(Extra.ID, audioId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.IDS, targetIds);
        return request;
    }
}

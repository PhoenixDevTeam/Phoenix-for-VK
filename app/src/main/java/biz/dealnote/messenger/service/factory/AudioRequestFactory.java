package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.service.IntArray;
import biz.dealnote.messenger.util.Objects;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_AUDIO;

public class AudioRequestFactory {

    public static final int REQUEST_ADD = 14001;
    public static final int REQUEST_DELETE = 14002;
    public static final int REQUEST_BROADCAST = 14003;
    public static final int REQUEST_RESTORE = 14004;
    public static final int REQUEST_FIND_COVER = 14005;

    public static Request getFindCoverRequest(int audioId, int ownerId, String artist, String title){
        Request request = new Request(REQUEST_FIND_COVER);
        request.put(Extra.ID, audioId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.ARTIST, artist);
        request.put(Extra.TITLE, title);
        return request;
    }

    public static Request getAddRequest(Audio audio, Integer groupId, Integer albumId){
        Request request = new Request(REQUEST_ADD);
        request.put(EXTRA_AUDIO, audio);
        if(Objects.nonNull(groupId)){
            request.put(Extra.GROUP_ID, groupId);
        }

        if(Objects.nonNull(albumId)){
            request.put(Extra.ALBUM_ID, albumId);
        }

        return request;
    }

    public static Request getDeleteRequest(int audioId, Integer ownerId){
        Request request = new Request(REQUEST_DELETE);
        request.put(Extra.ID, audioId);
        if(ownerId != null){
            request.put(Extra.OWNER_ID, ownerId);
        }
        return request;
    }

    public static Request getBroadcastRequest(int audioId, int ownerId, IntArray targetIds){
        Request request = new Request(REQUEST_BROADCAST);
        request.put(Extra.ID, audioId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.IDS, targetIds);
        return request;
    }

    public static Request getRestoreRequest(int audioId, Integer ownerId){
        Request request = new Request(REQUEST_RESTORE);
        request.put(Extra.ID, audioId);
        if(ownerId != null){
            request.put(Extra.OWNER_ID, ownerId);
        }
        return request;
    }

}

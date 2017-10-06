package biz.dealnote.messenger.service;

import android.support.annotation.NonNull;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.service.operations.photo.GetPhotoByIdOperation;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_MESSAGE_IDS;
import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_START_ID;

public final class RequestFactory {

    public static final int REQUEST_READ_MESSAGE = 9;

    public static final int REQUEST_DELETE_MESSAGES = 21;

    public static final int REQUEST_PHOTOS_BY_ID = 31;

    public static final int REQUEST_MESSAGES_RESTORE = 41;
    public static final int REQUEST_DOCS_DELETE = 42;

    public static Request getDocsDeleteRequest(int docId, int ownerId) {
        Request request = new Request(REQUEST_DOCS_DELETE);
        request.put(Extra.ID, docId);
        request.put(Extra.OWNER_ID, ownerId);
        return request;
    }

    public static Request getMessagesRestoreRequest(int accountId, int mid) {
        Request request = new Request(REQUEST_MESSAGES_RESTORE);
        request.put(Extra.ID, mid);
        request.put(Extra.ACCOUNT_ID, accountId);
        return request;
    }

    public static Request getPhotosByIdRequest(ArrayList<AccessIdPair> ids, boolean storeToDb) {
        Request request = new Request(REQUEST_PHOTOS_BY_ID);
        request.putParcelableArrayList(Extra.IDS, ids);
        request.put(GetPhotoByIdOperation.EXTRA_STORE_TO_DB, storeToDb);
        return request;
    }

    public static Request getReadMessageRequest(int accountId, IntArray mids, int peerId, Integer startMessageId) {
        Request request = new Request(REQUEST_READ_MESSAGE);
        request.put(Extra.IDS, mids);
        request.put(Extra.ACCOUNT_ID, accountId);
        request.put(Extra.PEER_ID, peerId);

        if(startMessageId != null){
            request.put(EXTRA_START_ID, startMessageId);
        }

        return request;
    }

    public static Request getDeleteMessageRequest(int accountId, @NonNull List<Integer> ids) {
        Request request = new Request(REQUEST_DELETE_MESSAGES);
        request.put(EXTRA_MESSAGE_IDS, new IntArray(ids));
        request.put(Extra.ACCOUNT_ID, accountId);
        return request;
    }

    private RequestFactory() {
    }
}
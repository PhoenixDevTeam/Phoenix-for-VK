package biz.dealnote.messenger.service;

import android.support.annotation.NonNull;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.service.operations.photo.GetPhotoByIdOperation;
import biz.dealnote.messenger.service.operations.poll.AddVoteOperation;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_ANSWER_ID;
import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_MESSAGE_IDS;
import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_START_ID;

public final class RequestFactory {

    public static final int REQUEST_DOCS_GET = 8;
    public static final int REQUEST_READ_MESSAGE = 9;
    public static final int REQUEST_LIKE = 12;

    public static final int REQUEST_DELETE_MESSAGES = 21;

    public static final int REQUEST_ADD_VOTE = 29;
    public static final int REQUEST_REMOVE_VOTE = 30;

    public static final int REQUEST_PHOTOS_BY_ID = 31;

    public static final int REQUEST_MESSAGES_RESTORE = 41;
    public static final int REQUEST_DOCS_DELETE = 42;
    public static final int REQUEST_DOCS_ADD = 43;

    public static Request getDocsAddRequest(int docId, int ownerId, String accessKey) {
        Request request = new Request(REQUEST_DOCS_ADD);
        request.put(Extra.ID, docId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.ACCESS_KEY, accessKey);
        return request;
    }

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

    public static Request getAddVoteRequest(Poll poll, int answerId) {
        Request request = new Request(REQUEST_ADD_VOTE);
        request.put(Extra.POLL, poll);
        request.put(AddVoteOperation.EXTRA_ANSWER_ID, answerId);
        //request.put(Extra.STORE_TO_DB, storeToDb);
        return request;
    }

    public static Request getRemoveVoteRequest(Poll poll, int answerId) {
        Request request = new Request(REQUEST_REMOVE_VOTE);
        request.put(Extra.POLL, poll);
        request.put(EXTRA_ANSWER_ID, answerId);
        //request.put(Extra.STORE_TO_DB, storeToDb);
        return request;
    }

    public static Request getLikeRequest(boolean add, int ownerId, int itemId, String type, String accessKey, boolean storeToDb) {
        Request request = new Request(REQUEST_LIKE);
        request.put(Extra.OWNER_ID, ownerId);
        request.put("add", add);
        request.put(Extra.ID, itemId);
        request.put(Extra.TYPE, type);
        request.put(Extra.ACCESS_KEY, accessKey);
        request.put(Extra.STORE_TO_DB, storeToDb);
        return request;
    }

    private RequestFactory() {
    }
}
package biz.dealnote.messenger.service;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.EXTRA_START_ID;

public final class RequestFactory {

    public static final int REQUEST_READ_MESSAGE = 9;


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

    private RequestFactory() {
    }
}
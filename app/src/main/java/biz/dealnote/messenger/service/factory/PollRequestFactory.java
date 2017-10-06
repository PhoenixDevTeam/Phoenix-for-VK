package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.service.StringArray;
import biz.dealnote.messenger.service.operations.poll.CreatePollOperation;
import biz.dealnote.messenger.service.operations.poll.GetPollByIdOperation;

public class PollRequestFactory {

    public static final int REQUEST_POLL_CREATE = 9001;
    public static final int REQUEST_GET_POLL_BY_ID = 9002;

    public static Request getCreatePollRequest(String question, boolean anonymous, Integer ownerId, List<String> options){
        Request request = new Request(REQUEST_POLL_CREATE);
        request.put(CreatePollOperation.EXTRA_QUESTION, question);
        request.put(CreatePollOperation.EXTRA_IS_ANOMYMOUS, anonymous);
        if(ownerId != null){
            request.put(Extra.OWNER_ID, ownerId);
        }

        request.put(CreatePollOperation.EXTRA_ADD_ANSWERS, new StringArray(options));
        return request;
    }

    public static Request getGetPollById(int pollId, int ownerId, boolean isBoard){
        Request request = new Request(REQUEST_GET_POLL_BY_ID);
        request.put(Extra.ID, pollId);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(GetPollByIdOperation.EXTRA_IS_BOARD, isBoard);
        //request.put(Extra.STORE_TO_DB, storeInDb);
        return request;
    }
}

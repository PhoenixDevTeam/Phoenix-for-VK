package biz.dealnote.messenger.service.operations.poll;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiPoll;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class GetPollByIdOperation extends AbsApiOperation {

    public static final String EXTRA_IS_BOARD = "is_board";

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);
        int ownerId = request.getInt(Extra.OWNER_ID);
        boolean isBoard = request.getBoolean(EXTRA_IS_BOARD);
        //boolean storeIdDb = request.getBoolean(Extra.STORE_TO_DB);

        VKApiPoll poll = Apis.get()
                .vkDefault(accountId)
                .polls()
                .getById(ownerId, isBoard, id)
                .blockingGet();

        poll.isBoard = isBoard;

        //if(storeIdDb){
        //    context.getContentResolver().insert(
        //            MessengerContentProvider.getPollContentUriFor(accountId),
        //            PollColumns.getCV(poll));
        //}

        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.POLL, Dto2Model.transform(poll));
        return bundle;
    }

}

package biz.dealnote.messenger.service.operations.groups;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class LeaveGroupOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int groupid = Math.abs(request.getInt(Extra.GROUP_ID));


        boolean success = Apis.get()
                .vkDefault(accountId)
                .groups()
                .leave(groupid)
                .blockingGet();

        return buildSimpleSuccessResult(success);
    }

}

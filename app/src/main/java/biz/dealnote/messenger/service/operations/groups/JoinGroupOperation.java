package biz.dealnote.messenger.service.operations.groups;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class JoinGroupOperation extends AbsApiOperation {

    public static final String EXTRA_NOT_SURE = "not_sure";

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int groupId = Math.abs(request.getInt(Extra.GROUP_ID));
        Integer notSure = request.optInt(EXTRA_NOT_SURE);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .groups()
                .join(groupId, notSure)
                .blockingGet();

        return buildSimpleSuccessResult(success);
    }
}

package biz.dealnote.messenger.service.operations.fave;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.FaveUsersColumns;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class FaveRemoveUserOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int userId = request.getInt(Extra.USER_ID);
        boolean success = Apis.get()
                .vkDefault(accountId)
                .fave()
                .removeUser(userId)
                .blockingGet();

        if (success) {
            context.getContentResolver().delete(MessengerContentProvider.getFaveUsersContentUriFor(accountId),
                    FaveUsersColumns._ID + " = ?", new String[]{String.valueOf(userId)});
        }

        return buildSimpleSuccessResult(success);
    }
}

package biz.dealnote.messenger.service.operations.message;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.column.DialogsColumns;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DeleteDialogOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int peerId = request.getInt(Extra.PEER_ID);
        int offset = request.getInt(Extra.OFFSET);
        int count = request.getInt(Extra.COUNT);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .messages()
                .deleteDialog(peerId, offset, count)
                .blockingGet();

        if (success) {
            context.getContentResolver().delete(MessengerContentProvider.getDialogsContentUriFor(accountId),
                    DialogsColumns._ID + " = ?", new String[]{String.valueOf(peerId)});
        }

        return buildSimpleSuccessResult(success);
    }
}

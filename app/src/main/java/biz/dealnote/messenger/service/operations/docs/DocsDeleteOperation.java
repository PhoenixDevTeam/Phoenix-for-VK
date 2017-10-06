package biz.dealnote.messenger.service.operations.docs;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.column.DocColumns;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DocsDeleteOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int id = request.getInt(Extra.ID);
        int ownerId = request.getInt(Extra.OWNER_ID);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .docs()
                .delete(ownerId, id)
                .blockingGet();

        if (success) {
            context.getContentResolver().delete(MessengerContentProvider.getDocsContentUriFor(accountId),
                    DocColumns.DOC_ID + " = ? AND " + DocColumns.OWNER_ID + " = ?",
                    new String[]{String.valueOf(id), String.valueOf(ownerId)});
        }

        return buildSimpleSuccessResult(success);
    }
}

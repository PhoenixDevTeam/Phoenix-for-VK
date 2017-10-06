package biz.dealnote.messenger.service.operations.fave;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.column.FaveLinksColumns;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class FaveRemoveLinkOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        String linkId = request.getString(EXTRA_LINK_ID);
        boolean success = Apis.get()
                .vkDefault(accountId)
                .fave()
                .removeLink(linkId)
                .blockingGet();

        if (success) {
            context.getContentResolver().delete(MessengerContentProvider.getFaveLinksContentUriFor(accountId),
                    FaveLinksColumns.FULL_LINK_ID + " LIKE ?", new String[]{linkId});
        }

        return buildSimpleSuccessResult(success);
    }
}

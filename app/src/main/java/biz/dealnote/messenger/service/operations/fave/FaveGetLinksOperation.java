package biz.dealnote.messenger.service.operations.fave;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.FaveLinksColumns;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Utils;

public class FaveGetLinksOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int count = request.getInt(Extra.COUNT);
        int offset = request.getInt(Extra.OFFSET);

        List<FaveLinkDto> links = Apis.get()
                .vkDefault(accountId)
                .fave()
                .getLinks(offset, count)
                .blockingGet()
                .getItems();

        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        if (offset == 0) {
            operations.add(ContentProviderOperation
                    .newDelete(MessengerContentProvider.getFaveLinksContentUriFor(accountId))
                    .build());
        }

        for (FaveLinkDto link : links) {
            operations.add(ContentProviderOperation
                    .newInsert(MessengerContentProvider.getFaveLinksContentUriFor(accountId))
                    .withValues(FaveLinksColumns.buildCV(link))
                    .build());
        }

        if (!Utils.safeIsEmpty(operations)) {
            context.getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(Extra.END_OF_CONTENT, count > links.size());
        return bundle;
    }
}

package biz.dealnote.messenger.service.operations.docs;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class DocsSearchOpearion extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        DocumentSearchCriteria criteria = (DocumentSearchCriteria) request.getParcelable(Extra.CRITERIA);
        int count = request.getInt(Extra.COUNT);
        int offset = request.getInt(Extra.OFFSET);

        List<VkApiDoc> dtos = Apis.get()
                .vkDefault(accountId)
                .docs()
                .search(criteria.getQuery(), count, offset)
                .blockingGet()
                .getItems();

        //List<VkApiDoc> dtos = api.searchDocuments(criteria.query, count, offset, null, null);

        ArrayList<Document> documents = new ArrayList<>(dtos.size());
        for (VkApiDoc dto : dtos) {
            documents.add(Dto2Model.transform(dto));
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Extra.DOCS, documents);
        return bundle;
    }
}

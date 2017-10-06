package biz.dealnote.messenger.service.operations.docs;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class DocsGetByIdOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int ownerId = request.getInt(Extra.OWNER_ID);
        int docId = request.getInt(Extra.ID);

        List<VkApiDoc> dtos = Apis.get()
                .vkDefault(accountId)
                .docs()
                .getById(Collections.singletonList(new IdPair(docId, ownerId)))
                .blockingGet();

        //List<VkApiDoc> documents = api.getDocsById(ownerId + "_" + docId);

        Bundle bundle = new Bundle();

        if(!safeIsEmpty(dtos)){
            for(VkApiDoc document : dtos){
                if(document.id == docId && document.ownerId == ownerId){
                    bundle.putParcelable(OUT_DOCUMENT, Dto2Model.transform(document));
                    break;
                }
            }
        }
        return bundle;
    }
}

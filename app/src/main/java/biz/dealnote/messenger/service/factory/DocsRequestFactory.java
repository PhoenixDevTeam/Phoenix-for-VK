package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;

public class DocsRequestFactory {

    public static final int REQUEST_DOC_GET_BY_ID = 19001;
    public static final int REQUEST_DOC_SEARCH = 19002;

    public static Request getGetDocByIdRequest(int ownerId, int docId){
        Request request = new Request(REQUEST_DOC_GET_BY_ID);
        request.put(Extra.OWNER_ID, ownerId);
        request.put(Extra.ID, docId);
        return request;
    }

    public static Request getDocsSearchRequest(DocumentSearchCriteria criteria, int count, int offset){
        Request request = new Request(REQUEST_DOC_SEARCH);
        request.put(Extra.CRITERIA, criteria);
        request.put(Extra.COUNT, count);
        request.put(Extra.OFFSET, offset);
        return request;
    }
}

package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.DocsAdapter;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.service.factory.DocsRequestFactory;
import biz.dealnote.messenger.util.Accounts;
import biz.dealnote.messenger.util.Utils;

public class DocumentsSearchFragment extends AbsSearchFragment<DocumentSearchCriteria, Document, IntNextFrom>
        implements DocsAdapter.ActionListener {

    public static DocumentsSearchFragment newInstance(int accountId, @Nullable DocumentSearchCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        DocumentsSearchFragment fragment = new DocumentsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected boolean canSearch(DocumentSearchCriteria mCriteria) {
        return !TextUtils.isEmpty(mCriteria.getQuery());
    }

    @Override
    protected DocumentSearchCriteria instantiateEmptyCriteria() {
        return new DocumentSearchCriteria("");
    }

    @Override
    protected IntNextFrom updateNextFrom(Request request, Bundle resultBundle) {
        int count = request.getInt(Extra.COUNT);
        int offset = request.getInt(Extra.OFFSET);
        return new IntNextFrom(count + offset);
    }

    @Override
    protected RecyclerView.Adapter createAdapter() {
        DocsAdapter adapter = new DocsAdapter(mData);
        adapter.setActionListner(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected Request buildRequest(int count, IntNextFrom nextFrom, DocumentSearchCriteria criteria) {
        return DocsRequestFactory.getDocsSearchRequest(criteria, count, nextFrom.getOffset());
    }

    @Override
    protected List<Document> parseResults(Bundle resultData) {
        return resultData.getParcelableArrayList(Extra.DOCS);
    }

    @Override
    protected int requestType() {
        return DocsRequestFactory.REQUEST_DOC_SEARCH;
    }

    @Override
    protected boolean isEndOfContent(@NonNull Request request, @NonNull Bundle resultData, List<Document> result) {
        return Utils.safeIsEmpty(result);
    }

    @NonNull
    @Override
    protected IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    public void onDocClick(int index, @NonNull Document doc) {
        PlaceFactory.getDocPreviewPlace(Accounts.fromArgs(getArguments()), doc).tryOpenWith(getActivity());
    }

    @Override
    public boolean onDocLongClick(int index, @NonNull Document doc) {
        return false;
    }
}

package biz.dealnote.messenger.fragment.fave;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.fave.FaveLinksAdapter;
import biz.dealnote.messenger.db.Repositories;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.fragment.base.AccountDependencyFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.service.factory.FaveRequestFactory;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import io.reactivex.disposables.CompositeDisposable;

public class FaveLinksFragment extends AccountDependencyFragment implements SwipeRefreshLayout.OnRefreshListener, FaveLinksAdapter.ClickListener {

    public static FaveLinksFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FaveLinksFragment fragment = new FaveLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FaveLinksAdapter mAdapter;
    private ArrayList<FaveLink> data;
    private boolean endOfContent;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_links, container, false);
        mRecyclerView = (RecyclerView) root.findViewById(android.R.id.list);
        mEmpty = (TextView) root.findViewById(R.id.empty);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                if (canLoadMore()) {
                    request(data.size());
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }

        boolean firstRun = false;
        if (data == null) {
            firstRun = true;
            data = new ArrayList<>();
        }

        mAdapter = new FaveLinksAdapter(data, getActivity());
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
           loadAll();
            request(0);
        }

        resolveEmptyText();
    }

    private void loadAll() {
        mCompositeDisposable.add(Repositories.getInstance()
                .fave()
                .getFaveLinks(getAccountId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLoadFinished, Analytics::logUnexpectedError));
    }

    private void onLoadFinished(List<FaveLink> links){
        data.clear();
        data.addAll(links);
        safeNotifyDataSetChanged();
        resolveEmptyText();
    }

    private boolean canLoadMore() {
        return !endOfContent
                && !Utils.safeIsEmpty(data)
                && !hasRequest(FaveRequestFactory.REQUEST_GET_LINKS);
    }

    private void resolveEmptyText() {
        if (!isAdded()) {
            return;
        }

        mEmpty.setVisibility(Utils.safeIsEmpty(data) ? View.VISIBLE : View.GONE);
    }

    private void request(int offset) {
        Request request = FaveRequestFactory.getGetLinksRequest(offset, 50);
        executeRequest(request);

        ViewUtils.showProgress(this, mSwipeRefreshLayout, true);
    }

    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_END_OF_CONTENT = "save_end_of_content";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, data);
        outState.putBoolean(SAVE_END_OF_CONTENT, endOfContent);
    }

    private void restoreFromInstanceState(Bundle state) {
        this.data = state.getParcelableArrayList(SAVE_DATA);
        this.endOfContent = state.getBoolean(SAVE_END_OF_CONTENT);
    }

    @Override
    protected void onRequestFinished(Request request, Bundle resultData) {
        super.onRequestFinished(request, resultData);

        if (request.getRequestType() == FaveRequestFactory.REQUEST_GET_LINKS) {
            ViewUtils.showProgress(this, mSwipeRefreshLayout, false);
            endOfContent = resultData.getBoolean(Extra.END_OF_CONTENT);
            loadAll();
        }

        if (request.getRequestType() == FaveRequestFactory.REQUEST_REMOVE_LINK) {
            if (!resultData.getBoolean(Extra.SUCCESS)) return;

            String linkId = request.getString(AbsApiOperation.EXTRA_LINK_ID);
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getId().equals(linkId)) {
                    data.remove(i);
                    if (isAdded()) {
                        mAdapter.notifyItemRemoved(i);
                    }

                    break;
                }
            }
        }
    }

    @Override
    protected void onRequestError(Request request, ServiceException throwable) {
        super.onRequestError(request, throwable);

        if(isAdded()){
            ViewUtils.showProgress(this, mSwipeRefreshLayout, false);
            Utils.showRedTopToast(getActivity(), throwable.getMessage());
        }
    }

    @Override
    public void onRefresh() {
        request(0);
    }

    private void safeNotifyDataSetChanged() {
        if (isAdded()) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLinkClick(int index, FaveLink link) {
        LinkHelper.openLinkInBrowser(getActivity(), link.getUrl());
    }

    @Override
    public void onLinkDelete(int index, FaveLink link) {
        Request request = FaveRequestFactory.getRemoveLinkRequest(link.getId());
        executeRequest(request);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}

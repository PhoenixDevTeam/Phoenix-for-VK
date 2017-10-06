package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.fragment.base.AccountDependencyFragment;
import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.AbsNextFrom;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * @param <C> Типа критерии для поиска
 * @param <A> Тип данных, которые будет в результатах поиска
 */
public abstract class AbsSearchFragment<C extends BaseSearchCriteria, A extends Parcelable, N extends AbsNextFrom>
        extends AccountDependencyFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = AbsSearchFragment.class.getSimpleName();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final int REQUEST_FILTER_EDIT = 16;

    protected C mCriteria;
    protected ArrayList<A> mData;
    protected boolean mEndOfContent;
    protected N mNextOffset;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected TextView mEmpty;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (nonNull(savedInstanceState)) {
            restoreFromSavedInstanceState(savedInstanceState);
        } else {
            mCriteria = getArguments().getParcelable(Extra.CRITERIA);
        }

        if(isNull(mCriteria)){
            mCriteria = instantiateEmptyCriteria();
        }

        if (isNull(mNextOffset)) {
            mNextOffset = getInitialNextFrom();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = root.findViewById(R.id.list);

        mEmpty = root.findViewById(R.id.empty);

        RecyclerView.LayoutManager manager = createLayoutManager();
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                if (canLoadMore()) {
                    runOrContinueSearch(false);
                }
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        resolveEmptyText();

        return root;
    }

    public void openSearchFilter(){
        FilterEditFragment fragment = FilterEditFragment.newInstance(getAccountId(), mCriteria.getOptions());
        fragment.setTargetFragment(this, REQUEST_FILTER_EDIT);
        fragment.show(getFragmentManager(), "filter-edit");
    }

    private void resolveEmptyText() {
        if (!isAdded()) return;

        boolean nowLoading = hasRequest(requestType());
        if (nowLoading || !Utils.safeIsEmpty(mData)) {
            mEmpty.setVisibility(View.GONE);
        } else {
            mEmpty.setVisibility(View.VISIBLE);

            boolean emptyQuery = mCriteria == null || Utils.safeTrimmedIsEmpty(mCriteria.getQuery());

            if (emptyQuery) {
                mEmpty.setText(R.string.enter_to_search);
            } else {
                mEmpty.setText(getString(R.string.nothing_found, mCriteria.getQuery().trim()));
            }
        }
    }

    private boolean canLoadMore() {
        return !mEndOfContent && !hasRequest(requestType());
    }

    protected abstract boolean canSearch(C mCriteria);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromSavedInstanceState(savedInstanceState);
        }

        boolean firtsRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firtsRun = true;
        }

        mAdapter = createAdapter();
        mRecyclerView.setAdapter(mAdapter);

        resolveEmptyText();

        if (firtsRun && canSearch(mCriteria)) {
            ignoreRequestResult(requestType());
            resetNextFrom();
            runOrContinueSearch(true);
        }
    }

    protected abstract C instantiateEmptyCriteria();

    /**
     * Хэшкод критерии, на которую на данный момент отображены результаты
     */
    private int mResultsForHash;

    public void setNewCriteria(String query) {
        this.mCriteria.setQuery(query.trim());
        onSearchCriteriaChanged();
    }

    private static final String SAVE_CRITERIA = "save_criteria";
    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_NEXT_OFFSET = "save_next_offset";
    private static final String SAVE_RESULTS_FOR_HASH = "save_results_for_hash";

    protected void restoreFromSavedInstanceState(@NonNull Bundle state) {
        this.mCriteria = state.getParcelable(SAVE_CRITERIA);
        this.mData = state.getParcelableArrayList(SAVE_DATA);
        this.mNextOffset = state.getParcelable(SAVE_NEXT_OFFSET);
        this.mResultsForHash = state.getInt(SAVE_RESULTS_FOR_HASH);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_CRITERIA, mCriteria);
        outState.putParcelableArrayList(SAVE_DATA, mData);
        outState.putParcelable(SAVE_NEXT_OFFSET, mNextOffset);
        outState.putInt(SAVE_RESULTS_FOR_HASH, mResultsForHash);
    }

    protected abstract N updateNextFrom(Request request, Bundle resultBundle);

    @Override
    protected void onRequestFinished(Request request, Bundle resultData) {
        super.onRequestFinished(request, resultData);
        if (request.getRequestType() == requestType()) {
            List<A> result = parseResults(resultData);
            mNextOffset = updateNextFrom(request, resultData);
            mEndOfContent = isEndOfContent(request, resultData, result);

            if (mEndOfContent) {
                Logger.d(TAG, "onRequestFinished, mEndOfContent!!!");
            }

            boolean atLast = request.getBoolean(Extra.AT_LAST);
            if (atLast) {
                mData.clear();
                mData.addAll(result);
                mAdapter.notifyDataSetChanged();
            } else {
                int startSize = mData.size();
                mData.addAll(result);
                mAdapter.notifyItemRangeInserted(startSize, result.size());
            }

            ViewUtils.showProgress(this, mSwipeRefreshLayout, false);

            resolveEmptyText();

            mResultsForHash = request.getInt("hash");
            Logger.d(TAG, "onRequestFinished, mResultsForHash: " + mResultsForHash);
        }
    }

    @Override
    protected void onRequestError(Request request, ServiceException throwable) {
        super.onRequestError(request, throwable);

        if (isAdded()) {
            Utils.showRedTopToast(getActivity(), throwable.getMessage());
            ViewUtils.showProgress(this, mSwipeRefreshLayout, false);
        }
    }

    private void onSearchCriteriaChanged() {
        int hash = mCriteria.hashCode();
        if (mResultsForHash == hash) {
            Logger.d(TAG, "onSearchCriteriaChanged, hash is equals to previous, search cancelled");
            return;
        }

        if (!isAdded() || !isResumed()) {
            return;
        }

        ignoreRequestResult(requestType());

        resetNextFrom();

        mData.clear();
        mAdapter.notifyDataSetChanged();

        HANDLER.removeCallbacks(search);

        if (canSearch(mCriteria)) {
            HANDLER.postDelayed(search, SEARCH_DELAY);
            mEmpty.setText(null);
        } else {
            resolveEmptyText();
        }
    }

    protected abstract RecyclerView.Adapter createAdapter();

    protected abstract RecyclerView.LayoutManager createLayoutManager();

    protected abstract Request buildRequest(int count, N nextFrom, C criteria);

    protected abstract List<A> parseResults(Bundle resultData);

    protected abstract int requestType();

    @Override
    public void onRefresh() {
        ignoreRequestResult(requestType());

        resetNextFrom();
        runOrContinueSearch(true);
    }

    @Override
    public void onDestroy() {
        HANDLER.removeCallbacks(search);
        super.onDestroy();
    }

    private Runnable search = () -> runOrContinueSearch(true);

    private static final int COUNT_PER_REQUEST = 50;
    private static final int SEARCH_DELAY = 1000;

    private void runOrContinueSearch(boolean atLast) {
        Logger.d(TAG, "runOrContinueSearch, offset: " + mNextOffset + ", criteria: " + mCriteria);

        Request request = buildRequest(getCountPerRequest(), mNextOffset, mCriteria);
        request.put("hash", mCriteria.hashCode());
        request.put(Extra.AT_LAST, atLast);

        executeRequest(request);
        ViewUtils.showProgress(this, mSwipeRefreshLayout, true);

        resolveEmptyText();
    }

    protected abstract boolean isEndOfContent(@NonNull Request request, @NonNull Bundle resultData, List<A> result);

    protected int getCountPerRequest() {
        return COUNT_PER_REQUEST;
    }

    public String getCurrentQuery() {
        return mCriteria == null ? null : mCriteria.getQuery();
    }

    public BaseSearchCriteria getCriteria() {
        return mCriteria;
    }

    private void resetNextFrom() {
        mNextOffset.reset();
    }

    @NonNull
    protected abstract N getInitialNextFrom();
}

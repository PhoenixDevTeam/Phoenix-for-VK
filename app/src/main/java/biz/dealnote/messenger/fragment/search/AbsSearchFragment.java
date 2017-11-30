package biz.dealnote.messenger.fragment.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.fragment.search.options.BaseOption;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.mvp.presenter.search.AbsSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IBaseSearchView;
import biz.dealnote.messenger.util.ViewUtils;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public abstract class AbsSearchFragment<P extends AbsSearchPresenter<V, ?, T, ?>, V extends IBaseSearchView<T>, T>
        extends PlaceSupportPresenterFragment<P, V> implements IBaseSearchView<T> {

    private static final int REQUEST_FILTER_EDIT = 19;
    private RecyclerView.Adapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mEmptyText;

    private void onSeachOptionsChanged(){
        getPresenter().fireOptionsChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.list);
        RecyclerView.LayoutManager manager = createLayoutManager();
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = createAdapter(Collections.emptyList());
        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        mEmptyText = root.findViewById(R.id.empty);
        mEmptyText.setText(getEmptyText());
        return root;
    }

    @StringRes
    int getEmptyText() {
        return R.string.list_is_empty;
    }

    public void fireTextQueryEdit(String q) {
        getPresenter().fireTextQueryEdit(q);
    }

    @Override
    public void displayData(List<T> data) {
        if (nonNull(mAdapter)) {
            setAdapterData(mAdapter, data);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    public static final String ACTION_QUERY = "action_query";

    /**
     * Метод будет вызван, когда внутри viewpager this фрагмент будет выбран
     */
    public void syncYourCriteriaWithParent() {
        getPresenter().fireSyncCriteriaRequest();
    }

    public void openSearchFilter(){
        getPresenter().fireOpenFilterClick();
    }

    @Override
    public void displayFilter(int accountId, ArrayList<BaseOption> options) {
        FilterEditFragment fragment = FilterEditFragment.newInstance(accountId, options);
        fragment.setTargetFragment(this, REQUEST_FILTER_EDIT);
        fragment.show(getFragmentManager(), "filter-edit");
    }

    @Override
    public void displaySearchQuery(String query) {
        Intent data = new Intent(ACTION_QUERY);
        data.putExtra(Extra.Q, query);

        if (nonNull(getTargetFragment())) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }

        if (nonNull(getParentFragment())) {
            getParentFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }
    }

    abstract void setAdapterData(RecyclerView.Adapter adapter, List<T> data);

    abstract RecyclerView.Adapter createAdapter(List<T> data);

    abstract RecyclerView.LayoutManager createLayoutManager();
}

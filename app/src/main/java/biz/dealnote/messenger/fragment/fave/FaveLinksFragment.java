package biz.dealnote.messenger.fragment.fave;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.fave.FaveLinksAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.mvp.presenter.FaveLinksPresenter;
import biz.dealnote.messenger.mvp.view.IFaveLinksView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class FaveLinksFragment extends BasePresenterFragment<FaveLinksPresenter, IFaveLinksView> implements IFaveLinksView, FaveLinksAdapter.ClickListener {

    public static FaveLinksFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FaveLinksFragment fragment = new FaveLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FaveLinksAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_links, container, false);

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        mEmpty = root.findViewById(R.id.empty);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        mAdapter = new FaveLinksAdapter(Collections.emptyList(), getActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if(Objects.nonNull(mEmpty) && Objects.nonNull(mAdapter)){
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onLinkClick(int index, FaveLink link) {
        getPresenter().fireLinkClick(link);
    }

    @Override
    public void openLink(int accountId, FaveLink link) {
        LinkHelper.openLinkInBrowser(getActivity(), link.getUrl());
    }

    @Override
    public void notifyItemRemoved(int index) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index);
            resolveEmptyText();
        }
    }

    @Override
    public void onLinkDelete(int index, FaveLink link) {
        getPresenter().fireDeleteClick(link);
    }

    @Override
    protected String tag() {
        return FaveLinksFragment.class.getSimpleName();
    }

    @Override
    public void displayLinks(List<FaveLink> links) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(links);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public IPresenterFactory<FaveLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FaveLinksPresenter(getArguments().getInt(Extra.ACCOUNT_ID),saveInstanceState);
    }
}
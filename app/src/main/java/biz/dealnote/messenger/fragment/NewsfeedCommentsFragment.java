package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.NewsfeedCommentsAdapter;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.NewsfeedComment;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.NewsfeedCommentsPresenter;
import biz.dealnote.messenger.mvp.view.INewsfeedCommentsView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isLandscape;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedCommentsFragment extends PlaceSupportPresenterFragment<NewsfeedCommentsPresenter, INewsfeedCommentsView>
        implements INewsfeedCommentsView, NewsfeedCommentsAdapter.ActionListener {

    public static NewsfeedCommentsFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsfeedCommentsFragment fragment = new NewsfeedCommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsfeedCommentsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_newsfeed_comments, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(getActivity())) {
            manager = new StaggeredGridLayoutManager(isLandscape(getActivity()) ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(getActivity());
        }

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new NewsfeedCommentsAdapter(getActivity(), Collections.emptyList(), this);
        mAdapter.setActionListener(this);
        mAdapter.setOwnerClickListener(this);

        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public IPresenterFactory<NewsfeedCommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            return new NewsfeedCommentsPresenter(accountId, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.NEWSFEED_COMMENTS);

        ActivityUtils.setToolbarTitle(this, R.string.drawer_newsfeed_comments);
        ActivityUtils.setToolbarSubtitle(this, null);

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onSectionResume(NavigationFragment.SECTION_ITEM_NEWSFEED_COMMENTS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    protected String tag() {
        return NewsfeedCommentsFragment.class.getSimpleName();
    }

    @Override
    public void displayData(List<NewsfeedComment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void onPostBodyClick(NewsfeedComment comment) {
        getPresenter().firePostClick((Post) comment.getModel());
    }

    @Override
    public void onCommentBodyClick(NewsfeedComment comment) {
        getPresenter().fireCommentBodyClick(comment);
    }
}
package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.TopicsAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Topic;
import biz.dealnote.messenger.mvp.presenter.TopicsPresenter;
import biz.dealnote.messenger.mvp.view.ITopicsView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.LoadMoreFooterHelper;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class TopicsFragment extends BasePresenterFragment<TopicsPresenter, ITopicsView>
        implements SwipeRefreshLayout.OnRefreshListener, ITopicsView, TopicsAdapter.ActionListener {

    public static Bundle buildArgs(int accountId, int ownerId){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        return args;
    }

    public static TopicsFragment newInstance(Bundle args){
        TopicsFragment fragment = new TopicsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TopicsFragment newInstance(int accountId, int ownerId){
        return newInstance(buildArgs(accountId, ownerId));
    }

    private TopicsAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LoadMoreFooterHelper helper;
    private FloatingActionButton fabCreate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_topics, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new TopicsAdapter(getActivity(), Collections.emptyList(), this);

        View footer = inflater.inflate(R.layout.footer_load_more, recyclerView, false);
        helper = LoadMoreFooterHelper.createFrom(footer, () -> getPresenter().fireLoadMoreClick());
        mAdapter.addFooter(footer);

        recyclerView.setAdapter(mAdapter);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        fabCreate = root.findViewById(R.id.fragment_topics_create);
        fabCreate.setOnClickListener(view -> getPresenter().fireButtonCreateClick());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(actionBar != null){
            actionBar.setTitle(R.string.topics);
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
        return TopicsFragment.class.getSimpleName();
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(@NonNull List<Topic> topics) {
        Logger.d(tag(), "displayData, size: " + topics.size());
        if(nonNull(mAdapter)){
            mAdapter.setItems(topics);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdd(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void setupLoadMore(@LoadMoreState int state) {
        if(nonNull(helper)){
            helper.switchToState(state);
        }
    }

    @Override
    public void goToComments(int accountId, @NonNull Topic topic) {
        PlaceFactory.getCommentsPlace(accountId, Commented.from(topic), null)
                .tryOpenWith(getActivity());
    }

    @Override
    public void setButtonCreateVisible(boolean visible) {
        if(nonNull(fabCreate)){
            if(visible && !fabCreate.isShown()){
                fabCreate.show();
            }

            if(!visible && fabCreate.isShown()){
                fabCreate.hide();
            }
        }
    }

    @Override
    public IPresenterFactory<TopicsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = getArguments().getInt(Extra.OWNER_ID);
            return new TopicsPresenter(accountId, ownerId, saveInstanceState);
        };
    }

    @Override
    public void onTopicClick(@NonNull Topic topic) {
        getPresenter().fireTopicClick(topic);
    }
}

package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.adapter.VideosAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.VideosListPresenter;
import biz.dealnote.messenger.mvp.view.IVideosListView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideosFragment extends BasePresenterFragment<VideosListPresenter, IVideosListView>
        implements IVideosListView, VideosAdapter.VideoOnClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String EXTRA_ALBUM_TITLE = "album_title";

    public static Bundle buildArgs(int accoutnId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accoutnId);
        args.putInt(Extra.ALBUM_ID, albumId);
        args.putInt(Extra.OWNER_ID, ownerId);
        if (albumTitle != null) {
            args.putString(EXTRA_ALBUM_TITLE, albumTitle);
        }

        args.putString(Extra.ACTION, action);
        return args;
    }

    @Override
    public IPresenterFactory<VideosListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int albumId = getArguments().getInt(Extra.ALBUM_ID);
            int ownerId = getArguments().getInt(Extra.OWNER_ID);

            String optAlbumTitle = getArguments().getString(EXTRA_ALBUM_TITLE);
            String action = getArguments().getString(Extra.ACTION);
            return new VideosListPresenter(accountId, ownerId, albumId, action, optAlbumTitle, saveInstanceState);
        };
    }

    public static VideosFragment newInstance(int accoutnId, int ownerId, int albumId, String action, @Nullable String albumTitle) {
        return newInstance(buildArgs(accoutnId, ownerId, albumId, action, albumTitle));
    }

    public static VideosFragment newInstance(Bundle args) {
        VideosFragment fragment = new VideosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * True - если фрагмент находится внутри TabLayout
     */
    private boolean inTabsContainer;

    private VideosAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = getArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (!inTabsContainer) {
            super.setToolbarTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        if (!inTabsContainer) {
            super.setToolbarSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setToolbarTitle(getString(R.string.videos));

        if (!inTabsContainer) {
            if (getActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) getActivity()).onClearSelection();
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setBlockNavigationDrawer(false)
                    .setStatusBarColored(true)
                    .build()
                    .apply(getActivity());
        }
    }

    @Override
    protected String tag() {
        return null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);

        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        int columns = getContext().getResources().getInteger(R.integer.videos_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAdapter = new VideosAdapter(getActivity(), Collections.emptyList());
        mAdapter.setVideoOnClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onVideoClick(int position, Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public void displayData(@NonNull List<Video> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(loading);
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void returnSelectionToParent(Video video) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, Utils.singletonArrayList(video));
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void showVideoPreview(int accountId, Video video) {
        PlaceFactory.getVideoPreviewPlace(accountId, video).tryOpenWith(getActivity());
    }
}

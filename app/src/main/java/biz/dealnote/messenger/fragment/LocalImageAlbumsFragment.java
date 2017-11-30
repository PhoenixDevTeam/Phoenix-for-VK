package biz.dealnote.messenger.fragment;

import android.Manifest;
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

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.LocalPhotoAlbumsAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.mvp.presenter.LocalPhotoAlbumsPresenter;
import biz.dealnote.messenger.mvp.view.ILocalPhotoAlbumsView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class LocalImageAlbumsFragment extends BasePresenterFragment<LocalPhotoAlbumsPresenter, ILocalPhotoAlbumsView>
        implements LocalPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener, ILocalPhotoAlbumsView {

    private static final int REQYEST_PERMISSION_READ_EXTERNAL_STORAGE = 89;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyTextView;

    private LocalPhotoAlbumsAdapter mAlbumsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_albums_gallery, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if(!hasHideToolbarExtra()){
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        RecyclerView.LayoutManager manager = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotoAlbumsAdapter.PICASSO_TAG));

        mAlbumsAdapter = new LocalPhotoAlbumsAdapter(Collections.emptyList());
        mAlbumsAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAlbumsAdapter);

        mEmptyTextView = (TextView)view.findViewById(R.id.empty);
        return view;
    }

    @Override
    public void onClick(LocalImageAlbum album) {
        getPresenter().fireAlbumClick(album);
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(@NonNull List<LocalImageAlbum> data) {
        if(Objects.nonNull(mRecyclerView)){
            mAlbumsAdapter.setData(data);
        }
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if(Objects.nonNull(mEmptyTextView)){
            mEmptyTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayProgress(boolean loading) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    @Override
    public void openAlbum(@NonNull LocalImageAlbum album) {
        PlaceFactory.getLocalImageAlbumPlace(album).tryOpenWith(getActivity());
    }

    @Override
    public void notifyDataChanged() {
        if(Objects.nonNull(mAlbumsAdapter)){
            mAlbumsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQYEST_PERMISSION_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQYEST_PERMISSION_READ_EXTERNAL_STORAGE){
            getPresenter().fireReadExternalStoregePermissionResolved();
        }
    }

    @Override
    protected String tag() {
        return LocalImageAlbumsFragment.class.getSimpleName();
    }

    @Override
    public void savePresenterState(@NonNull LocalPhotoAlbumsPresenter presenter, @NonNull Bundle outState) {
        presenter.saveState(outState);
    }

    @Override
    public IPresenterFactory<LocalPhotoAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LocalPhotoAlbumsPresenter(saveInstanceState);
    }
}
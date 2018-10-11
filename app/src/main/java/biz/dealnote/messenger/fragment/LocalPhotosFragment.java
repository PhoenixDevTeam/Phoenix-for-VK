package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.LocalPhotosAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.mvp.presenter.LocalPhotosPresenter;
import biz.dealnote.messenger.mvp.view.ILocalPhotosView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class LocalPhotosFragment extends BaseMvpFragment<LocalPhotosPresenter, ILocalPhotosView>
        implements ILocalPhotosView, LocalPhotosAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String EXTRA_MAX_SELECTION_COUNT = "max_selection_count";

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LocalPhotosAdapter mAdapter;
    private TextView mEmptyTextView;
    private FloatingActionButton fabAttach;

    public static LocalPhotosFragment newInstance(int maxSelectionItemCount, LocalImageAlbum album) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_MAX_SELECTION_COUNT, maxSelectionItemCount);
        args.putParcelable(Extra.ALBUM, album);
        LocalPhotosFragment fragment = new LocalPhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
        RecyclerView.LayoutManager manager = new GridLayoutManager(getActivity(), columnCount);

        mRecyclerView = view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(LocalPhotosAdapter.TAG));

        mEmptyTextView = view.findViewById(R.id.empty);

        fabAttach = view.findViewById(R.id.fr_photo_gallery_attach);
        fabAttach.setOnClickListener(v -> getPresenter().fireFabClick());

        return view;
    }

    @Override
    public void onPhotoClick(LocalPhotosAdapter.ViewHolder holder, LocalPhoto photo) {
        getPresenter().firePhotoClick(photo);
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(@NonNull List<LocalPhoto> data) {
        mAdapter = new LocalPhotosAdapter(getActivity(), data);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
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
    public void returnResultToParent(ArrayList<LocalPhoto> photos) {
        Collections.sort(photos);

        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.PHOTOS, photos);

        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void updateSelectionAndIndexes() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.updateHoldersSelectionAndIndexes();
        }
    }

    @Override
    public void setFabVisible(boolean visible, boolean anim) {
        if (visible && !fabAttach.isShown()) {
            fabAttach.show();
        }

        if (!visible && fabAttach.isShown()) {
            fabAttach.hide();
        }
    }

    @Override
    public void showError(String text) {
        if(isAdded()) Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(@StringRes int titleRes, Object... params) {
        if(isAdded()) showError(getString(titleRes, params));
    }

    @Override
    public IPresenterFactory<LocalPhotosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int maxSelectionItemCount1 = requireArguments().getInt(EXTRA_MAX_SELECTION_COUNT, 10);
            LocalImageAlbum album = requireArguments().getParcelable(Extra.ALBUM);
            AssertUtils.requireNonNull(album);
            return new LocalPhotosPresenter(album, maxSelectionItemCount1, saveInstanceState);
        };
    }
}
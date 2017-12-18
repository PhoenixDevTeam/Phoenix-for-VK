package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.VkPhotoAlbumsAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoAlbumEditor;
import biz.dealnote.messenger.mvp.presenter.PhotoAlbumsPresenter;
import biz.dealnote.messenger.mvp.view.IPhotoAlbumsView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class VKPhotoAlbumsFragment extends BasePresenterFragment<PhotoAlbumsPresenter, IPhotoAlbumsView> implements IPhotoAlbumsView,
        VkPhotoAlbumsAdapter.ClickListener, SwipeRefreshLayout.OnRefreshListener {

    public static final String ACTION_SELECT_ALBUM = "biz.dealnote.messenger.ACTION_SELECT_ALBUM";

    public static VKPhotoAlbumsFragment newInstance(int accountId, int ownerId, String action, ParcelableOwnerWrapper ownerWrapper) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putParcelable(Extra.OWNER, ownerWrapper);
        args.putString(Extra.ACTION, action);

        VKPhotoAlbumsFragment fragment = new VKPhotoAlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFab;
    private VkPhotoAlbumsAdapter mAdapter;
    private TextView mEmptyText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums_gallery, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if(!hasHideToolbarExtra()){
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = view.findViewById(R.id.list);

        mEmptyText = view.findViewById(R.id.empty);

        int columnCount = getResources().getInteger(R.integer.photos_albums_column_count);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));

        mAdapter = new VkPhotoAlbumsAdapter(getActivity(), Collections.emptyList());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(v -> getPresenter().fireCreateAlbumClick());
        return view;
    }

    private static final int REQUEST_CREATE_ALBUM = 134;
    private static final int REQUEST_EDIT_ALBUM = 138;

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.VK_PHOTO_ALBUMS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(R.string.photos);
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
        return VKPhotoAlbumsFragment.class.getCanonicalName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if (requestCode == REQUEST_CREATE_ALBUM) {
        //    VKApiPhotoAlbum album = data.getParcelableExtra(Extra.ALBUM);
        //    AssertUtils.requireNonNull(album);
        //    this.mData.add(0, album);
        //    safeNotifyDatasetChanged();
        //}

        //if (requestCode == REQUEST_EDIT_ALBUM) {
        //    VKApiPhotoAlbum album = data.getParcelableExtra(Extra.ALBUM);
        //    AssertUtils.requireNonNull(album);
        //    replacePhotoAlbum(album);
        //}
    }

    //private void replacePhotoAlbum(@NonNull VKApiPhotoAlbum album) {
    //    for (int i = 0; i < mData.size(); i++) {
    //        if (mData.get(i).equals(album)) {
    //            mData.set(i, album);
     //           if (mAdapter != null) mAdapter.notifyItemChanged(i);
    //            break;
    //        }
    //    }
    //}

    @Override
    public void showDeleteConfirmDialog(@NonNull final PhotoAlbum album) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_confirm)
                .setMessage(R.string.album_remove_confirm_message)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> getPresenter().fireAlbumDeletingConfirmed(album))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(@NonNull List<PhotoAlbum> data) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    private void resolveEmptyTextVisibility() {
        if(Objects.nonNull(mEmptyText) && Objects.nonNull(mAdapter)){
            mEmptyText.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(R.string.photos);
        }
    }

    @Override
    public void openAlbum(int accountId, @NonNull PhotoAlbum album, @Nullable Owner owner, @Nullable String action) {
        PlaceFactory.getVKPhotosAlbumPlace(accountId, album.getOwnerId(), album.getId(), action)
                .withParcelableExtra(Extra.ALBUM, album)
                .withParcelableExtra(Extra.OWNER, new ParcelableOwnerWrapper(owner))
                .tryOpenWith(getActivity());
    }

    @Override
    public void showAlbumContextMenu(@NonNull PhotoAlbum album) {
        String[] items = {getString(R.string.delete), getString(R.string.edit)};
        new AlertDialog.Builder(getActivity())
                .setTitle(album.getTitle())
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            getPresenter().fireAlbumDeleteClick(album);
                            break;
                        case 1:
                            getPresenter().fireAlbumEditClick(album);
                            break;
                    }
                })
                .show();
    }

    @Override
    public void doSelection(@NonNull PhotoAlbum album) {
        Intent result = new Intent();
        result.putExtra(Extra.OWNER_ID, album.getOwnerId());
        result.putExtra(Extra.ALBUM_ID, album.getId());
        getActivity().setResult(Activity.RESULT_OK, result);
        getActivity().finish();
    }

    @Override
    public void setCreateAlbumFabVisible(boolean visible) {
        if(Objects.isNull(mFab)) return;

        if (mFab.isShown() && !visible) {
            mFab.hide();
        }

        if (!mFab.isShown() && visible) {
            mFab.show();
        }
    }

    @Override
    public void goToAlbumCreation(int accountId, int ownerId) {
        PlaceFactory.getCreatePhotoAlbumPlace(accountId, ownerId)
                .targetTo(this, REQUEST_CREATE_ALBUM)
                .tryOpenWith(getActivity());
    }

    @Override
    public void goToAlbumEditing(int accountId, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor) {
        PlaceFactory.getEditPhotoAlbumPlace(accountId, album, editor)
                //.withParcelableExtra(Extra.OWNER, owner)
                .targetTo(this, REQUEST_EDIT_ALBUM)
                .tryOpenWith(getActivity());
    }

    @Override
    public void seDrawertPhotoSectionActive(boolean active) {
        if (getActivity() instanceof OnSectionResumeCallback) {
            if (active) {
                ((OnSectionResumeCallback) getActivity()).onSectionResume(NavigationFragment.SECTION_ITEM_PHOTOS);
            } else {
                ((OnSectionResumeCallback) getActivity()).onClearSelection();
            }
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int size) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, size);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public IPresenterFactory<PhotoAlbumsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int ownerId = getArguments().getInt(Extra.OWNER_ID);
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);

            ParcelableOwnerWrapper wrapper = getArguments().getParcelable(Extra.OWNER);
            Owner owner = Objects.nonNull(wrapper) ? wrapper.get() : null;

            String action = getArguments().getString(Extra.ACTION);
            return new PhotoAlbumsPresenter(accountId, ownerId, new PhotoAlbumsPresenter.AdditionalParams()
                    .setAction(action)
                    .setOwner(owner), saveInstanceState);
        };
    }

    @Override
    public void onVkPhotoAlbumClick(@NonNull PhotoAlbum album) {
        getPresenter().fireAlbumClick(album);
    }

    @Override
    public boolean onVkPhotoAlbumLongClick(@NonNull PhotoAlbum album) {
        return getPresenter().fireAlbumLongClick(album);
    }
}
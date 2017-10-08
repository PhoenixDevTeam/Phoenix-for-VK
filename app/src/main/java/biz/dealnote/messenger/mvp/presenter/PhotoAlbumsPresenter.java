package biz.dealnote.messenger.mvp.presenter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.IPhotosInteractor;
import biz.dealnote.messenger.domain.IUtilsInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.VKPhotoAlbumsFragment;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoAlbumEditor;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IPhotoAlbumsView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.findIndexById;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by ruslan.kolbasa on 29.11.2016.
 * phoenix
 */
public class PhotoAlbumsPresenter extends AccountDependencyPresenter<IPhotoAlbumsView> {

    private static final String TAG = PhotoAlbumsPresenter.class.getSimpleName();

    private int mOwnerId;
    private Owner mOwner;
    private String mAction;
    private ArrayList<PhotoAlbum> mData;

    private final IPhotosInteractor photosInteractor;
    private final IOwnersInteractor ownersInteractor;
    private final IUtilsInteractor utilsInteractor;

    public PhotoAlbumsPresenter(int accountId, int ownerId, @Nullable AdditionalParams params, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownersInteractor = InteractorFactory.createOwnerInteractor();
        this.photosInteractor = InteractorFactory.createPhotosInteractor();
        this.utilsInteractor = InteractorFactory.createUtilsInteractor();

        mOwnerId = ownerId;

        //do restore this

        if (Objects.nonNull(params)) {
            mAction = params.getAction();
        }

        if (Objects.isNull(mOwner) && Objects.nonNull(params)) {
            mOwner = params.getOwner();
        }

        if (Objects.isNull(mData)) {
            mData = new ArrayList<>();

            loadAllFromDb();
            refreshFromNet(0);
        }

        if (Objects.isNull(mOwner) && !isMy()) {
            loadOwnerInfo();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mData);
    }

    @OnGuiCreated
    private void resolveDrawerPhotoSection() {
        if (isGuiReady()) {
            getView().seDrawertPhotoSectionActive(isMy());
        }
    }

    private boolean isMy() {
        return mOwnerId == getAccountId();
    }

    private void loadOwnerInfo() {
        if (isMy()) {
            return;
        }

        final int accountId = super.getAccountId();
        appendDisposable(ownersInteractor.getBaseOwnerInfo(accountId, mOwnerId, IOwnersInteractor.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onOwnerInfoReceived, this::onOwnerGetError));
    }

    private void onOwnerGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onOwnerInfoReceived(Owner owner) {
        this.mOwner = owner;
        resolveSubtitleView();
        resolveCreateAlbumButtonVisibility();
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean netLoadingNow;

    private void refreshFromNet(int offset) {
        this.netLoadingNow = true;
        resolveProgressView();

        final int accountId = super.getAccountId();
        netDisposable.add(photosInteractor.getActualAlbums(accountId, mOwnerId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(albums -> onActualAlbumsReceived(offset, albums), this::onActualAlbumsGetError));
    }

    private void onActualAlbumsGetError(Throwable t) {
        this.netLoadingNow = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveProgressView();
    }

    private void onActualAlbumsReceived(int offset, List<PhotoAlbum> albums) {
        // reset cache loading
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.netLoadingNow = false;

        if (offset == 0) {
            this.mData.clear();
            this.mData.addAll(albums);
            callView(IPhotoAlbumsView::notifyDataSetChanged);
        } else {
            int startSize = this.mData.size();
            this.mData.addAll(albums);
            callView(view -> view.notifyDataAdded(startSize, albums.size()));
        }

        resolveProgressView();
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheLoadingNow;

    private void loadAllFromDb() {
        this.cacheLoadingNow = true;

        final int accountId = super.getAccountId();
        cacheDisposable.add(photosInteractor.getCachedAlbums(accountId, mOwnerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, t -> {/*ignored*/}));
    }

    private void onCachedDataReceived(List<PhotoAlbum> albums) {
        this.cacheLoadingNow = false;

        this.mData.clear();
        this.mData.addAll(albums);

        safeNotifyDatasetChanged();
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @OnGuiCreated
    private void resolveProgressView() {
        if (isGuiReady()) {
            getView().displayLoading(netLoadingNow);
        }
    }


    private void safeNotifyDatasetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    @OnGuiCreated
    private void resolveSubtitleView() {
        if (isGuiReady()) {
            getView().setToolbarSubtitle(Objects.isNull(mOwner) || isMy() ? null : mOwner.getFullName());
        }
    }


    @Override
    protected String tag() {
        return TAG;
    }

    private void doAlbumRemove(@NonNull PhotoAlbum album) {
        final int accountId = super.getAccountId();
        final int albumId = album.getId();
        final int ownerId = album.getOwnerId();

        appendDisposable(photosInteractor.removedAlbum(accountId, album.getOwnerId(), album.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onAlbumRemoved(albumId, ownerId), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onAlbumRemoved(int albumId, int ownerId) {
        int index = findIndexById(this.mData, albumId, ownerId);
        if (index != -1) {
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireCreateAlbumClick() {
        getView().goToAlbumCreation(getAccountId(), mOwnerId);
    }

    public void fireAlbumClick(PhotoAlbum album) {
        if (VKPhotoAlbumsFragment.ACTION_SELECT_ALBUM.equals(mAction)) {
            getView().doSelection(album);
        } else {
            getView().openAlbum(getAccountId(), album, mOwner, mAction);
        }
    }

    public boolean fireAlbumLongClick(PhotoAlbum album) {
        if (canDeleteOrEdit(album)) {
            getView().showAlbumContextMenu(album);
            return true;
        }

        return false;
    }

    private boolean isAdmin() {
        return mOwner instanceof Community && ((Community) mOwner).isAdmin();
    }

    private boolean canDeleteOrEdit(@NonNull PhotoAlbum album) {
        return !album.isSystem() && (isMy() || isAdmin());
    }

    @OnGuiCreated
    private void resolveCreateAlbumButtonVisibility() {
        if (isGuiReady()) {
            boolean mustBeVisible = isMy() || isAdmin();
            getView().setCreateAlbumFabVisible(mustBeVisible);
        }
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.netDisposable.clear();
        this.netLoadingNow = false;

        refreshFromNet(0);
    }

    public void fireAlbumDeletingConfirmed(PhotoAlbum album) {
        doAlbumRemove(album);
    }

    public void fireAlbumDeleteClick(PhotoAlbum album) {
        getView().showDeleteConfirmDialog(album);
    }

    public void fireAlbumEditClick(PhotoAlbum album) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, SimplePrivacy> privacies = new HashMap<>();

        privacies.put(0, album.getPrivacyView());
        privacies.put(1, album.getPrivacyComment());

        final int accountId = super.getAccountId();

        appendDisposable(utilsInteractor
                .createFullPrivacies(accountId, privacies)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(full -> {
                    PhotoAlbumEditor editor = PhotoAlbumEditor.create()
                            .setPrivacyView(full.get(0))
                            .setPrivacyComment(full.get(1))
                            .setTitle(album.getTitle())
                            .setDescription(album.getDescription())
                            .setCommentsDisabled(album.isCommentsDisabled())
                            .setUploadByAdminsOnly(album.isUploadByAdminsOnly());
                    if (isGuiReady()) {
                        getView().goToAlbumEditing(getAccountId(), album, editor);
                    }
                }, Analytics::logUnexpectedError));
    }

    public static class AdditionalParams {

        private Owner owner;
        private String action;

        public AdditionalParams setOwner(Owner owner) {
            this.owner = owner;
            return this;
        }

        private Owner getOwner() {
            return owner;
        }

        public AdditionalParams setAction(String action) {
            this.action = action;
            return this;
        }

        private String getAction() {
            return action;
        }
    }
}
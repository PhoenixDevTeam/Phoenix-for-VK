package biz.dealnote.messenger.mvp.presenter.photo;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IPhotosInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IPhotoPagerView;
import biz.dealnote.messenger.task.DownloadImageTask;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;

import static biz.dealnote.messenger.util.Utils.findIndexById;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class PhotoPagerPresenter extends AccountDependencyPresenter<IPhotoPagerView> {

    private static final String TAG = PhotoPagerPresenter.class.getSimpleName();

    private static final String SAVE_INDEX = "save-index";
    private static final String SAVE_DATA = "save-data";

    ArrayList<Photo> mPhotos;
    private int mCurrentIndex;
    private boolean mLoadingNow;
    private boolean mFullScreen;

    final IPhotosInteractor photosInteractor;

    PhotoPagerPresenter(@NonNull ArrayList<Photo> initialData, int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.photosInteractor = InteractorFactory.createPhotosInteractor();

        if(Objects.nonNull(savedInstanceState)){
            mCurrentIndex = savedInstanceState.getInt(SAVE_INDEX);
        }

        initPhotosData(initialData, savedInstanceState);

        AssertUtils.requireNonNull(mPhotos, "'mPhotos' not initialized");
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_INDEX, mCurrentIndex);
        savePhotosState(outState);
    }

    void savePhotosState(@NonNull Bundle outState){
        outState.putParcelableArrayList(SAVE_DATA, mPhotos);
    }

    void initPhotosData(@NonNull ArrayList<Photo> initialData, @Nullable Bundle savedInstanceState){
        if (savedInstanceState == null) {
            mPhotos = initialData;
        } else {
            mPhotos = savedInstanceState.getParcelableArrayList(SAVE_DATA);
        }
    }

    void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveLoadingView();
    }

    private void resolveLoadingView() {
        if (isGuiReady()) {
            getView().displayPhotoListLoading(mLoadingNow);
        }
    }

    void refreshPagerView() {
        if (isGuiReady()) {
            getView().displayPhotos(mPhotos, mCurrentIndex);
        }
    }

    void setCurrentIndex(int currentIndex) {
        this.mCurrentIndex = currentIndex;
    }

    @NonNull
    protected ArrayList<Photo> getData() {
        return mPhotos;
    }

    @Override
    public void onViewHostAttached(@NonNull IPhotoPagerView viewHost) {
        super.onViewHostAttached(viewHost);
        resolveOptionMenu();
    }

    private void resolveOptionMenu() {
        if (isViewHostAttached()) {
            getView().setupOptionMenu(canSaveYourself(), canDelete());
        }
    }

    private boolean canDelete() {
        return hasPhotos() && getCurrent().getOwnerId() == getAccountId();
    }

    private boolean canSaveYourself() {
        return hasPhotos() && getCurrent().getOwnerId() != getAccountId();
    }

    @Override
    public void onGuiCreated(@NonNull IPhotoPagerView viewHost) {
        super.onGuiCreated(viewHost);
        getView().displayPhotos(mPhotos, mCurrentIndex);

        refreshInfoViews();
        resolveRestoreButtonVisibility();
        resolveToolbarVisibility();
        resolveButtonsBarVisible();
        resolveLoadingView();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public final void firePageSelected(int position) {
        int old = mCurrentIndex;
        changePageTo(position);
        afterPageChangedFromUi(old, position);
    }

    protected void afterPageChangedFromUi(int oldPage, int newPage) {

    }

    void changePageTo(int position) {
        if (mCurrentIndex == position) return;

        mCurrentIndex = position;
        onPositionChanged();
    }

    private void resolveLikeView() {
        if (isGuiReady() && hasPhotos()) {
            Photo photo = getCurrent();
            getView().setupLikeButton(photo.isUserLikes(), photo.getLikesCount());
        }
    }

    private void resolveCommentsView() {
        if (isGuiReady() && hasPhotos()) {
            Photo photo = getCurrent();
            boolean visible = photo.isCanComment() || photo.getCommentsCount() > 0;
            getView().setupCommentsButton(visible, photo.getCommentsCount());
        }
    }

    int count() {
        return mPhotos.size();
    }

    void resolveToolbarTitleSubtitleView() {
        if (!isGuiReady() || !hasPhotos()) return;

        String title = App.getInstance().getString(R.string.image_number, mCurrentIndex + 1, count());
        getView().setToolbarTitle(title);
        getView().setToolbarSubtitle(getCurrent().getText());
    }

    @NonNull
    private Photo getCurrent() {
        return mPhotos.get(mCurrentIndex);
    }

    private void onPositionChanged() {
        refreshInfoViews();
        resolveRestoreButtonVisibility();
        resolveOptionMenu();
    }

    public void fireInfoButtonClick() {
        String info = getCurrent().getText();
        String time = AppTextUtils.getDateFromUnixTime(getCurrent().getDate());
        getView().showPhotoInfo(time, info);
    }

    public void fireShareButtonClick() {
        Photo current = getCurrent();
        getView().sharePhoto(getAccountId(), current);
    }

    public void firePostToMyWallClick() {
        Photo photo = getCurrent();
        getView().postToMyWall(photo, getAccountId());
    }

    void refreshInfoViews() {
        resolveToolbarTitleSubtitleView();
        resolveLikeView();
        resolveCommentsView();
    }

    public void fireLikeClick() {
        addOrRemoveLike();
    }

    private void addOrRemoveLike() {
        final Photo photo = getCurrent();

        final int ownerId = photo.getOwnerId();
        final int photoId = photo.getId();
        final int accountId = super.getAccountId();
        final boolean add = !photo.isUserLikes();

        appendDisposable(photosInteractor.like(accountId, ownerId, photoId, add, photo.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(count -> interceptLike(ownerId, photoId, count, add), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onDeleteOrRestoreResult(int photoId, int ownerId, boolean deleted) {
        int index = findIndexById(this.mPhotos, photoId, ownerId);

        if(index != -1){
            Photo photo = mPhotos.get(index);
            photo.setDeleted(deleted);

            if (mCurrentIndex == index) {
                resolveRestoreButtonVisibility();
            }
        }
    }

    private void interceptLike(int ownerId, int photoId, int count, boolean userLikes) {
        for (Photo photo : mPhotos) {
            if (photo.getId() == photoId && photo.getOwnerId() == ownerId) {
                photo.setLikesCount(count);
                photo.setUserLikes(userLikes);
                resolveLikeView();
                break;
            }
        }
    }

    public void fireSaveOnDriveClick() {
        if (!AppPerms.hasWriteStoragePermision(App.getInstance())) {
            getView().requestWriteToExternalStoragePermission();
            return;
        }

        doSaveOnDrive();
    }

    private void doSaveOnDrive() {
        String dcim = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(dcim + "/" + Constants.PHOTOS_PATH);
        if (!dir.isDirectory()) {
            boolean created = dir.mkdirs();
            if (!created) {
                safeShowError(getView(), "Can't create directory " + dir);
                return;
            }
        }

        Photo photo = getCurrent();
        String file = dir.getAbsolutePath() + "/" + photo.getOwnerId() + "_" + photo.getId() + ".jpg";
        String url = photo.getUrlForSize(PhotoSize.W, true);

        new InternalDownloader(this, getApplicationContext(), url, file).doDownload();
    }

    private static final class InternalDownloader extends DownloadImageTask {

        final WeakReference<PhotoPagerPresenter> ref;

        InternalDownloader(PhotoPagerPresenter presenter, Context context, String url, String file) {
            super(context, url, file);
            this.ref = new WeakReference<>(presenter);
        }

        @Override
        protected void onPostExecute(String s) {
            PhotoPagerPresenter presenter = ref.get();

            if (Objects.isNull(presenter)) return;

            if (Objects.isNull(s)) {
                presenter.safeShowLongToast(presenter.getView(), R.string.saved);
            } else {
                presenter.safeShowLongToast(presenter.getView(), R.string.error_with_message, s);
            }
        }
    }

    public void fireSaveYourselfClick() {
        final Photo photo = getCurrent();
        final int accountId = super.getAccountId();

        appendDisposable(photosInteractor.copy(accountId, photo.getOwnerId(), photo.getId(), photo.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignored -> onPhotoCopied(), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onPhotoCopied() {
        safeShowLongToast(getView(), R.string.photo_saved_yourself);
    }

    public void fireDeleteClick() {
        delete();
    }

    public void fireWriteExternalStoragePermissionResolved() {
        if (AppPerms.hasWriteStoragePermision(App.getInstance())) {
            doSaveOnDrive();
        }
    }

    public void fireButtonRestoreClick() {
        restore();
    }

    private void resolveRestoreButtonVisibility() {
        if (isGuiReady()) {
            getView().setButtonRestoreVisible(hasPhotos() && getCurrent().isDeleted());
        }
    }

    private void restore() {
        deleteOrRestore(false);
    }

    private void deleteOrRestore(boolean detele){
        final Photo photo = getCurrent();
        final int photoId = photo.getId();
        final int ownerId = photo.getOwnerId();
        final int accountId = super.getAccountId();

        Completable completable;
        if(detele){
            completable = photosInteractor.deletePhoto(accountId, ownerId, photoId);
        } else {
            completable = photosInteractor.restorePhoto(accountId, ownerId, photoId);
        }

        appendDisposable(completable.compose(RxUtils.applyCompletableIOToMainSchedulers())
        .subscribe(() -> onDeleteOrRestoreResult(photoId, ownerId, detele), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void delete() {
        deleteOrRestore(true);
    }

    public void fireCommentsButtonClick() {
        Photo photo = getCurrent();
        getView().goToComments(getAccountId(), Commented.from(photo));
    }

    private boolean hasPhotos() {
        return !Utils.safeIsEmpty(mPhotos);
    }

    public void firePhotoTap() {
        if (!hasPhotos()) return;

        mFullScreen = !mFullScreen;

        resolveToolbarVisibility();
        resolveButtonsBarVisible();
    }

    void resolveButtonsBarVisible() {
        if (isGuiReady()) {
            getView().setButtonsBarVisible(hasPhotos() && !mFullScreen);
        }
    }

    void resolveToolbarVisibility() {
        if (isGuiReady()) {
            getView().setToolbarVisible(hasPhotos() && !mFullScreen);
        }
    }

    int getCurrentIndex() {
        return mCurrentIndex;
    }

    public void fireLikeLongClick() {
        if (!hasPhotos()) return;

        Photo photo = getCurrent();
        getView().goToLikesList(getAccountId(), photo.getOwnerId(), photo.getId());
    }
}
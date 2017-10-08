package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.ILocalPhotoAlbumsView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public class LocalPhotoAlbumsPresenter extends RxSupportPresenter<ILocalPhotoAlbumsView> {

    private static final String TAG = LocalPhotoAlbumsPresenter.class.getSimpleName();

    private boolean permissionRequestedOnce;
    private List<LocalImageAlbum> mLocalImageAlbums;

    public LocalPhotoAlbumsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        mLocalImageAlbums = new ArrayList<>();
    }

    private boolean mLoadingNow;

    @Override
    public void onGuiCreated(@NonNull ILocalPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);

        if(!AppPerms.hasReadStoragePermision(getApplicationContext())){
            if(!permissionRequestedOnce){
                permissionRequestedOnce = true;
                getView().requestReadExternalStoragePermission();
            }
        } else {
            loadData();
        }

        getView().displayData(mLocalImageAlbums);
        resolveProgressView();
        resolveEmptyTextView();
    }

    private void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveProgressView();
    }

    private void resolveProgressView() {
        if (isGuiReady()) getView().displayProgress(mLoadingNow);
    }

    private void loadData() {
        if (mLoadingNow) return;

        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .localPhotos()
                .getImageAlbums()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataLoaded, this::onLoadError));
    }

    private void onLoadError(Throwable throwable) {
        Analytics.logUnexpectedError(throwable);
        changeLoadingNowState(false);
    }

    private void onDataLoaded(List<LocalImageAlbum> data) {
        changeLoadingNowState(false);
        mLocalImageAlbums.clear();
        mLocalImageAlbums.addAll(data);

        if (isGuiReady()) {
            getView().notifyDataChanged();
        }

        resolveEmptyTextView();
    }

    private void resolveEmptyTextView() {
        if (isGuiReady()) getView().setEmptyTextVisible(Utils.safeIsEmpty(mLocalImageAlbums));
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireRefresh() {
        loadData();
    }

    public void fireAlbumClick(@NonNull LocalImageAlbum album) {
        getView().openAlbum(album);
    }

    public void fireReadExternalStoregePermissionResolved() {
        if(AppPerms.hasReadStoragePermision(getApplicationContext())){
            loadData();
        }
    }
}

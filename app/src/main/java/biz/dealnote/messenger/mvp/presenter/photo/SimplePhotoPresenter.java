package biz.dealnote.messenger.mvp.presenter.photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RxUtils;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class SimplePhotoPresenter extends PhotoPagerPresenter {

    private boolean mDataRefreshSuccessfull;

    public SimplePhotoPresenter(@NonNull ArrayList<Photo> photos, int index, boolean needToRefreshData,
                                int accountId, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, savedInstanceState);

        if (savedInstanceState == null) {
            setCurrentIndex(index);
        } else {
            mDataRefreshSuccessfull = savedInstanceState.getBoolean(SAVE_DATA_REFRESH_RESULT);
        }

        if (needToRefreshData && !mDataRefreshSuccessfull) {
            refreshData();
        }
    }

    private void refreshData() {
        final ArrayList<AccessIdPair> ids = new ArrayList<>(getData().size());
        final int accountId = super.getAccountId();

        for (Photo photo : getData()) {
            ids.add(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey()));
        }

        appendDisposable(photosInteractor.getPhotosByIds(accountId, ids)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPhotosReceived, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void onPhotosReceived(List<Photo> photos) {
        mDataRefreshSuccessfull = true;
        onPhotoListRefresh(photos);
    }

    private static final String TAG = SimplePhotoPresenter.class.getSimpleName();

    @Override
    protected String tag() {
        return TAG;
    }

    private void onPhotoListRefresh(@NonNull List<Photo> photos) {
        List<Photo> originalData = super.getData();

        for (Photo photo : photos) {
            //замена старых обьектов новыми
            for (int i = 0; i < originalData.size(); i++) {
                Photo orig = originalData.get(i);

                if (orig.getId() == photo.getId() && orig.getOwnerId() == photo.getOwnerId()) {
                    originalData.set(i, photo);

                    // если у фото до этого не было ссылок на файлы
                    if (isGuiReady() && (isNull(orig.getSizes()) || orig.getSizes().isEmpty())) {
                        Logger.d(TAG, "Rebind holder at " + i);

                        getView().rebindPhotoAt(i);
                    }
                    break;
                }
            }
        }

        super.refreshInfoViews();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBoolean(SAVE_DATA_REFRESH_RESULT, mDataRefreshSuccessfull);
    }

    private static final String SAVE_DATA_REFRESH_RESULT = "save-data-refresh-result";
}
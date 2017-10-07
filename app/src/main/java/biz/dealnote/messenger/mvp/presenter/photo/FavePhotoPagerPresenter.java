package biz.dealnote.messenger.mvp.presenter.photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.util.RxUtils;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 28.09.2016.
 * phoenix
 */
public class FavePhotoPagerPresenter extends PhotoPagerPresenter {

    private static final String SAVE_UPDATED = "save_updated";

    private boolean[] mUpdated;
    private boolean[] refreshing;

    public FavePhotoPagerPresenter(@NonNull ArrayList<Photo> photos, int index, int accountId, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, savedInstanceState);
        this.refreshing = new boolean[photos.size()];

        if (savedInstanceState == null) {
            mUpdated = new boolean[photos.size()];
            setCurrentIndex(index);
            refresh(index);
        } else {
            mUpdated = savedInstanceState.getBooleanArray(SAVE_UPDATED);
        }
    }

    private void refresh(int index) {
        if (mUpdated[index] || refreshing[index]) {
            return;
        }

        this.refreshing[index] = true;

        final Photo photo = getData().get(index);
        final int accountId = super.getAccountId();

        List<AccessIdPair> forUpdate = Collections.singletonList(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey()));
        appendDisposable(photosInteractor.getPhotosByIds(accountId, forUpdate)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(photos -> onPhotoUpdateReceived(photos, index), t -> onRefreshFailed(index, t)));
    }

    private void onRefreshFailed(int index, Throwable t) {
        this.refreshing[index] = false;
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onPhotoUpdateReceived(List<Photo> result, int index) {
        this.refreshing[index] = false;

        if (result.size() == 1) {
            final Photo p = result.get(0);

            getData().set(index, p);

            mUpdated[index] = true;

            if (getCurrentIndex() == index) {
                refreshInfoViews();
            }
        }
    }

    @Override
    protected void afterPageChangedFromUi(int oldPage, int newPage) {
        super.afterPageChangedFromUi(oldPage, newPage);
        refresh(newPage);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBooleanArray(SAVE_UPDATED, mUpdated);
    }
}
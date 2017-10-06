package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public interface ILocalPhotosView extends IMvpView, IErrorView {
    void displayData(@NonNull List<LocalPhoto> data);
    void setEmptyTextVisible(boolean visible);
    void displayProgress(boolean loading);
    void returnResultToParent(ArrayList<LocalPhoto> photos);
    void updateSelectionAndIndexes();
    void setFabVisible(boolean visible, boolean anim);
}

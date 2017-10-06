package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public interface IPhotoPagerView extends IMvpView, IAccountDependencyView, IErrorView, IToastView {

    void goToLikesList(int accountId, int ownerId, int photoId);
    void setupLikeButton(boolean like, int likes);
    void setupCommentsButton(boolean visible, int count);
    void displayPhotos(@NonNull List<Photo> photos, int initialIndex);
    void setToolbarTitle(String title);
    void setToolbarSubtitle(String subtitle);
    void sharePhoto(int accountId, @NonNull Photo photo);
    void postToMyWall(@NonNull Photo photo, int accountId);
    void showPhotoInfo(String time, String info);
    void requestWriteToExternalStoragePermission();
    void setButtonRestoreVisible(boolean visible);
    void setupOptionMenu(boolean canSaveYourself, boolean canDelete);
    void goToComments(int accountId, @NonNull Commented commented);

    void displayPhotoListLoading(boolean loading);
    void setButtonsBarVisible(boolean visible);
    void setToolbarVisible(boolean visible);
    void rebindPhotoAt(int position);
}

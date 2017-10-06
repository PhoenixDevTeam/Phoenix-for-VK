package biz.dealnote.messenger.mvp.view;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 05.12.2016.
 * phoenix
 */
public interface IBaseAttachmentsEditView extends IMvpView, IAccountDependencyView, IErrorView {

    void displayInitialModels(@NonNull List<AttachmenEntry> models);

    void setSupportedButtons(boolean photo, boolean audio, boolean video, boolean doc, boolean poll, boolean timer);

    void setTextBody(CharSequence text);

    void openAddVkPhotosWindow(int maxSelectionCount, int accountId, int ownerId);

    void openAddPhotoFromGalleryWindow(int maxSelectionCount);

    void openCamera(@NonNull Uri photoCameraUri);

    void openAddAudiosWindow(int maxSelectionCount, int accountId);

    void openAddDocumentsWindow(int maxSelectionCount, int accountId);

    void openAddVideosWindow(int maxSelectionCount, int accountId);

    void openPollCreationWindow(int accountId, int ownerId);

    void requestReadExternalStoragePermission();

    void requestCameraPermission();

    void displayChoosePhotoTypeDialog();

    void notifySystemAboutNewPhoto(@NonNull Uri uri);

    void displaySelectUploadPhotoSizeDialog(@NonNull List<LocalPhoto> photos);

    void notifyDataSetChanged();

    void updateProgressAtIndex(int index, int progress);

    void setTimerValue(Long time);

    void showEnterTimeDialog(long initialTimeUnixtime);

    void notifyItemRangeInsert(int position, int count);

    void notifyItemRemoved(int position);
}

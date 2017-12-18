package biz.dealnote.messenger.mvp.view;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public interface IMessageAttachmentsView extends IMvpView, IErrorView {

    void displayAttachments(List<AttachmenEntry> entries);

    void notifyDataAdded(int positionStart, int count);

    void addPhoto(int accountId, int ownerId);

    void notifyEntryRemoved(int index);

    void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos);

    void changePercentageSmoothly(int dataPosition, int progress);

    void notifyItemChanged(int index);

    void setEmptyViewVisible(boolean visible);

    void requestCameraPermission();

    void startCamera(@NonNull Uri fileUri);

    void syncAccompanyingWithParent(ModelsBundle accompanying);

    void startAddDocumentActivity(int accountId);

    void startAddVideoActivity(int accountId, int ownerId);
}

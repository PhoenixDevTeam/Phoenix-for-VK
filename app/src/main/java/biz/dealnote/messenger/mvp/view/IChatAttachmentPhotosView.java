package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public interface IChatAttachmentPhotosView extends IBaseChatAttachmentsView<Photo> {
    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);
}
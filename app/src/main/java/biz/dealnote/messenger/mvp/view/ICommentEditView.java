package biz.dealnote.messenger.mvp.view;

import android.support.annotation.Nullable;

import biz.dealnote.messenger.model.Comment;

/**
 * Created by admin on 06.05.2017.
 * phoenix
 */
public interface ICommentEditView extends IBaseAttachmentsEditView, IProgressView {
    void goBackWithResult(@Nullable Comment comment);
    void showConfirmWithoutSavingDialog();

    void goBack();
}

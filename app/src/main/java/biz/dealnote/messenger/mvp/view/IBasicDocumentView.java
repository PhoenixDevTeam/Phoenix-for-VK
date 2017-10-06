package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public interface IBasicDocumentView extends IMvpView, IAccountDependencyView, IToastView, IErrorView {

    void shareDocument(int accountId, @NonNull Document document);
    void requestWriteExternalStoragePermission();

}

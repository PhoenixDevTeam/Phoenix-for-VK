package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IDocsInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IBasicDocumentView;
import biz.dealnote.messenger.util.RxUtils;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 27.09.2016.
 * phoenix
 */
public class BaseDocumentPresenter<V extends IBasicDocumentView> extends AccountDependencyPresenter<V> {

    private static final String TAG = BaseDocumentPresenter.class.getSimpleName();

    private final IDocsInteractor docsInteractor;

    public BaseDocumentPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.docsInteractor = InteractorFactory.createDocsInteractor();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public final void fireWritePermissionResolved() {
        onWritePermissionResolved();
    }

    protected void onWritePermissionResolved() {
        // hook for child classes
    }

    protected void addYourself(@NonNull Document document) {
        final int accountId = super.getAccountId();
        final int docId = document.getId();
        final int ownerId = document.getOwnerId();

        appendDisposable(docsInteractor.add(accountId, docId, ownerId, document.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(id -> onDocAddedSuccessfully(docId, ownerId, id), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    protected void delete(int id, int ownerId) {
        final int accountId = super.getAccountId();
        appendDisposable(docsInteractor.delete(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDocDeleteSuccessfully(id, ownerId), this::onDocDeleteError));
    }

    private void onDocDeleteError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    @SuppressWarnings("unused")
    protected void onDocDeleteSuccessfully(int id, int ownerId) {
        safeShowLongToast(getView(), R.string.deleted);
    }

    @SuppressWarnings("unused")
    protected void onDocAddedSuccessfully(int id, int ownerId, int resultDocId) {
        safeShowLongToast(getView(), R.string.added);
    }
}
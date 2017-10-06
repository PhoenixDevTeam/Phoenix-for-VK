package biz.dealnote.messenger.mvp.presenter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.IBasicDocumentView;
import biz.dealnote.messenger.service.factory.DocsRequestFactory;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public class BasicSingleDocumentPresenter<V extends IBasicDocumentView> extends BaseDocumentPresenter<V> {

    private static final String TAG = BasicSingleDocumentPresenter.class.getSimpleName();

    private int mDocId;
    private int mDocOwnerId;

    private Document mDocument;

    public BasicSingleDocumentPresenter(int accountId, @NonNull Document doc, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mDocument = doc;
        mDocId = doc.getId();
        mDocOwnerId = doc.getOwnerId();
    }

    public BasicSingleDocumentPresenter(int accountId, int docId, int ownerId, @Nullable Bundle savedInstanceState){
        super(accountId, savedInstanceState);
        mDocId = docId;
        mDocOwnerId = ownerId;

        if(Objects.isNull(mDocument)){
            requestDocInfo();
        }
    }

    private void requestDocInfo(){
        Request request = DocsRequestFactory.getGetDocByIdRequest(mDocOwnerId, mDocId);
        executeRequest(request);
    }

    @Override
    protected void onRequestError(@NonNull Request request, @NonNull ServiceException e) {
        super.onRequestError(request, e);
        if(request.getRequestType() == DocsRequestFactory.REQUEST_DOC_GET_BY_ID){
            // TODO: 12.10.2016
        }
    }

    @Override
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {
        super.onRequestFinished(request, resultData);
        if(request.getRequestType() == DocsRequestFactory.REQUEST_DOC_GET_BY_ID){
            Document document = resultData.getParcelable(AbsApiOperation.OUT_DOCUMENT);

            if(Objects.nonNull(document)){
                mDocument = document;
                onDocumentInitialized();
            } else {
                onUnableToGetDocInfo();
            }
        }
    }

    @Nullable
    protected Document getDocument() {
        return mDocument;
    }

    @Override
    protected String tag() {
        return TAG;
    }

    protected boolean isMy(){
        return getAccountId() == mDocOwnerId;
    }

    protected void onUnableToGetDocInfo(){

    }

    protected void onDocumentInitialized(){

    }

    public void fireAddYourselfClick(){
        if(Objects.isNull(mDocument)){
            return;
        }

        addYourself(mDocument);
    }

    protected void fireDeleteClick() {
        delete(mDocId, mDocOwnerId);
    }

    public void fireShareButtonClick() {
        if(Objects.nonNull(mDocument)){
            getView().shareDocument(getAccountId(), mDocument);
        }
    }

    public void fireDownloadButtonClick() {
        if(Objects.isNull(mDocument)){
            return;
        }

        if(!AppPerms.hasWriteStoragePermision(App.getInstance())){
            getView().requestWriteExternalStoragePermission();
            return;
        }

        downloadImpl();
    }

    @Override
    public void onWritePermissionResolved() {
        if(AppPerms.hasWriteStoragePermision(App.getInstance())){
            downloadImpl();
        }
    }

    private void downloadImpl(){
        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(mDocument.getUrl()));
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mDocument.getTitle());
        req.allowScanningByMediaScanner();
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager dm = (DownloadManager) App.getInstance().getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(req);
    }
}

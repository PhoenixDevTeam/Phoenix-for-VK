package biz.dealnote.messenger.mvp.presenter;

import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IDocsInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.DocFilter;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IDocListView;
import biz.dealnote.messenger.upload.IUploadManager;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadResult;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.DisposableHolder;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.Injection.provideMainThreadScheduler;
import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.findIndexById;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class DocsListPresenter extends AccountDependencyPresenter<IDocListView> {

    private static final String SAVE_FILTER = "save_filter";

    public static final String ACTION_SELECT = "biz.dealnote.messenger.select.docs";
    public static final String ACTION_SHOW = "biz.dealnote.messenger.show.docs";

    private final int mOwnerId;
    private final DisposableHolder<Integer> mLoader = new DisposableHolder<>();
    private final List<Document> mDocuments;
    private final String mAction;

    private UploadDestination destination;
    private List<Upload> uploadsData;

    private final List<DocFilter> filters;
    private final IDocsInteractor docsInteractor;
    private final IUploadManager uploadManager;

    public DocsListPresenter(int accountId, int ownerId, @Nullable String action, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.docsInteractor = InteractorFactory.createDocsInteractor();
        this.uploadManager = Injection.provideUploadManager();

        this.mOwnerId = ownerId;

        this.mDocuments = new ArrayList<>();
        this.uploadsData = new ArrayList<>(0);
        this.mAction = action;

        this.destination = UploadDestination.forDocuments(ownerId);

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsDataReceived));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadDeleted));

        appendDisposable(uploadManager.observeResults()
                .filter(pair -> destination.compareTo(pair.getFirst().getDestination()))
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onProgressUpdates));

        int filter = isNull(savedInstanceState) ? DocFilter.Type.ALL : savedInstanceState.getInt(SAVE_FILTER);
        this.filters = createFilters(filter);

        loadAll();
        requestAll();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_FILTER, getSelectedFilter());
    }

    private List<DocFilter> createFilters(int selectedType) {
        List<DocFilter> data = new ArrayList<>();
        data.add(new DocFilter(DocFilter.Type.ALL, R.string.doc_filter_all));
        data.add(new DocFilter(DocFilter.Type.TEXT, R.string.doc_filter_text));
        data.add(new DocFilter(DocFilter.Type.ARCHIVE, R.string.doc_filter_archive));
        data.add(new DocFilter(DocFilter.Type.GIF, R.string.doc_filter_gif));
        data.add(new DocFilter(DocFilter.Type.IMAGE, R.string.doc_filter_image));
        data.add(new DocFilter(DocFilter.Type.AUDIO, R.string.doc_filter_audio));
        data.add(new DocFilter(DocFilter.Type.VIDEO, R.string.doc_filter_video));
        data.add(new DocFilter(DocFilter.Type.BOOKS, R.string.doc_filter_books));
        data.add(new DocFilter(DocFilter.Type.OTHER, R.string.doc_filter_other));

        for (DocFilter filter : data) {
            filter.setActive(selectedType == filter.getType());
        }

        return data;
    }

    private void onUploadsDataReceived(List<Upload> data) {
        uploadsData.clear();
        uploadsData.addAll(data);

        callView(IDocListView::notifyDataSetChanged);
        resolveUploadDataVisiblity();
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        mDocuments.add(0, (Document) pair.getSecond().getResult());
        callView(IDocListView::notifyDataSetChanged);
    }

    private void onProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for(IUploadManager.IProgressUpdate update : updates){
            int index = findIndexById(uploadsData, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(index, update.getProgress(), true));
            }
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploadsData, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadsAdded(List<Upload> added) {
        for (Upload u : added) {
            if (destination.compareTo(u.getDestination())) {
                int index = uploadsData.size();
                uploadsData.add(u);
                callView(view -> view.notifyUploadItemsAdded(index, 1));
            }
        }

        resolveUploadDataVisiblity();
    }

    private void onUploadDeleted(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploadsData, id);
            if (index != -1) {
                uploadsData.remove(index);
                callView(view -> view.notifyUploadItemRemoved(index));
            }
        }

        resolveUploadDataVisiblity();
    }

    @OnGuiCreated
    private void resolveUploadDataVisiblity() {
        if (isGuiReady()) {
            getView().setUploadDataVisible(!uploadsData.isEmpty());
        }
    }

    private DisposableHolder<Integer> requestHolder = new DisposableHolder<>();

    private boolean requestNow;
    private boolean cacheLoadingNow;

    private void setCacheLoadingNow(boolean cacheLoadingNow) {
        this.cacheLoadingNow = cacheLoadingNow;
        resolveRefreshingView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private int getSelectedFilter() {
        for (DocFilter filter : filters) {
            if (filter.isActive()) {
                return filter.getType();
            }
        }

        return DocFilter.Type.ALL;
    }

    private void requestAll() {
        setRequestNow(true);

        final int filter = getSelectedFilter();
        final int accountId = getAccountId();

        requestHolder.append(docsInteractor.request(accountId, mOwnerId, filter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNetDataReceived, throwable -> onRequestError(getCauseIfRuntime(throwable))));
    }

    private void onRequestError(Throwable throwable) {
        setRequestNow(false);
        showError(getView(), throwable);
    }

    private void onCacheDataReceived(List<Document> data) {
        setCacheLoadingNow(false);

        this.mDocuments.clear();
        this.mDocuments.addAll(data);

        safelyNotifyDataSetChanged();
    }

    private void onNetDataReceived(List<Document> data) {
        // cancel db loading if active
        mLoader.dispose();

        this.cacheLoadingNow = false;
        this.requestNow = false;

        resolveRefreshingView();

        this.mDocuments.clear();
        this.mDocuments.addAll(data);

        safelyNotifyDataSetChanged();
    }

    @Override
    public void onGuiCreated(@NonNull IDocListView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayUploads(uploadsData);
        viewHost.displayFilterData(filters);
    }

    private void loadAll() {
        setCacheLoadingNow(true);

        final int accountId = getAccountId();
        final int filter = getSelectedFilter();

        mLoader.append(docsInteractor.getCacheData(accountId, mOwnerId, filter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCacheDataReceived, throwable -> onLoadError(getCauseIfRuntime(throwable))));
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(isNowLoading());
        }
    }

    private boolean isNowLoading() {
        return cacheLoadingNow || requestNow;
    }

    private void safelyNotifyDataSetChanged() {
        resolveDocsListData();
    }

    @OnGuiCreated
    private void resolveDocsListData() {
        if (isGuiReady()) {
            getView().displayData(mDocuments, isImagesOnly());
        }
    }

    private boolean isImagesOnly() {
        return Utils.intValueIn(getSelectedFilter(), DocFilter.Type.IMAGE, DocFilter.Type.GIF);
    }

    private void onLoadError(Throwable throwable) {
        throwable.printStackTrace();
        setCacheLoadingNow(false);

        showError(getView(), throwable);

        resolveRefreshingView();
    }

    @Override
    public void onDestroyed() {
        mLoader.dispose();
        requestHolder.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        mLoader.dispose();
        this.cacheLoadingNow = false;

        requestAll();
    }

    public void fireButtonAddClick() {
        if (AppPerms.hasReadStoragePermision(getApplicationContext())) {
            getView().startSelectUploadFileActivity(getAccountId());
        } else {
            getView().requestReadExternalStoragePermission();
        }
    }

    public void fireDocClick(@NonNull Document doc) {
        if (ACTION_SELECT.equals(mAction)) {
            ArrayList<Document> selected = new ArrayList<>(1);
            selected.add(doc);

            getView().returnSelection(selected);
        } else {
            if (doc.isGif() && doc.hasValidGifVideoLink()) {
                ArrayList<Document> gifs = new ArrayList<>();
                int selectedIndex = 0;
                for (int i = 0; i < mDocuments.size(); i++) {
                    Document d = mDocuments.get(i);

                    if (d.isGif() && d.hasValidGifVideoLink()) {
                        gifs.add(d);
                    }

                    if (d == doc) {
                        selectedIndex = gifs.size() - 1;
                    }
                }

                getView().goToGifPlayer(getAccountId(), gifs, selectedIndex);
            } else {
                getView().openDocument(getAccountId(), doc);
            }
        }
    }

    public void fireReadPermissionResolved() {
        if (AppPerms.hasReadStoragePermision(getApplicationContext())) {
            getView().startSelectUploadFileActivity(getAccountId());
        }
    }

    public void fireFileForUploadSelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), destination)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    public void fireRemoveClick(Upload upload) {
        uploadManager.cancel(upload.getId());
    }

    public void fireFilterClick(DocFilter entry) {
        for (DocFilter filter : filters) {
            filter.setActive(entry.getType() == filter.getType());
        }

        getView().notifyFiltersChanged();

        loadAll();
        requestAll();
    }

    public void pleaseNotifyViewAboutAdapterType() {
        getViewHost().setAdapterType(isImagesOnly());
    }

    public void fireLocalPhotosForUploadSelected(ArrayList<LocalPhoto> photos) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, Upload.IMAGE_SIZE_FULL, true);
        uploadManager.enqueue(intents);
    }
}
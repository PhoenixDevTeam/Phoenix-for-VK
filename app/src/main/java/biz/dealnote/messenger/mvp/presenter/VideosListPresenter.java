package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.IVideosInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IVideosListView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class VideosListPresenter extends AccountDependencyPresenter<IVideosListView> {

    private static final String TAG = VideosListPresenter.class.getSimpleName();

    private static final int COUNT = 50;

    private final int ownerId;
    private final int albumId;

    private final String action;

    private String albumTitle;
    private final List<Video> data;

    private boolean endOfContent;

    private IntNextFrom intNextFrom;

    private final IVideosInteractor interactor;

    private boolean hasActualNetData;

    public VideosListPresenter(int accountId, int ownerId, int albumId, String action,
                               @Nullable String albumTitle, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.interactor = InteractorFactory.createVideosInteractor();

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;
        this.albumTitle = albumTitle;

        this.intNextFrom = new IntNextFrom(0);

        this.data = new ArrayList<>();

        loadAllFromCache();
        request(false);
    }

    private boolean requestNow;

    private void resolveRefreshingView(){
        if(isGuiResumed()){
            getView().displayLoading(requestNow);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private CompositeDisposable netDisposable = new CompositeDisposable();

    private void request(boolean more){
        if(requestNow) return;

        setRequestNow(true);

        int accountId = super.getAccountId();

        final IntNextFrom startFrom = more ? this.intNextFrom : new IntNextFrom(0);

        netDisposable.add(interactor.get(accountId, ownerId, albumId, COUNT, startFrom.getOffset())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + COUNT);
                    onRequestResposnse(videos, startFrom, nextFrom);
                }, throwable -> onListGetError(Utils.getCauseIfRuntime(throwable))));
    }

    private void onListGetError(Throwable throwable){
        setRequestNow(false);
        showError(getView(), throwable);
    }

    private void onRequestResposnse(List<Video> videos, IntNextFrom startFrom, IntNextFrom nextFrom){
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.hasActualNetData = true;
        this.intNextFrom = nextFrom;
        this.endOfContent = videos.isEmpty();

        if(startFrom.getOffset() == 0){
            data.clear();
            data.addAll(videos);

            callView(IVideosListView::notifyDataSetChanged);
        } else {
            if(nonEmpty(videos)){
                int startSize = data.size();
                data.addAll(videos);
                callView(view -> view.notifyDataAdded(startSize, videos.size()));
            }
        }

        setRequestNow(false);
    }

    @Override
    public void onGuiCreated(@NonNull IVideosListView view) {
        super.onGuiCreated(view);
        view.displayData(data);
        view.setToolbarSubtitle(albumTitle);
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheNowLoading;

    private void loadAllFromCache() {
        this.cacheNowLoading = true;
        final int accountId = super.getAccountId();

        cacheDisposable.add(interactor.getCachedVideos(accountId, ownerId, albumId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Video> videos){
        this.data.clear();
        this.data.addAll(videos);

        callView(IVideosListView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheNowLoading = false;

        this.netDisposable.clear();

        request(false);
    }

    private boolean canLoadMore(){
        return !endOfContent && !requestNow && hasActualNetData && !cacheNowLoading && nonEmpty(data);
    }

    public void fireScrollToEnd() {
        if(canLoadMore()){
            request(true);
        }
    }

    public void fireVideoClick(Video video) {
        if(IVideosListView.ACTION_SELECT.equalsIgnoreCase(action)){
            getView().returnSelectionToParent(video);
        } else {
            getView().showVideoPreview(getAccountId(), video);
        }
    }
}
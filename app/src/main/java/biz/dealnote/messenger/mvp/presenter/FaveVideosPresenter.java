package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.IFaveInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IFaveVideosView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class FaveVideosPresenter extends AccountDependencyPresenter<IFaveVideosView> {

    private static final String TAG = FaveVideosPresenter.class.getSimpleName();
    private static final int COUNT_PER_REQUEST = 25;

    private ArrayList<Video> mVideos;
    private boolean mEndOfContent;
    private final IFaveInteractor faveInteractor;

    public FaveVideosPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.faveInteractor = InteractorFactory.createFaveInteractor();
        mVideos = new ArrayList<>();

        loadCachedData();
        requestAtLast();
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private CompositeDisposable netDisposable = new CompositeDisposable();

    private boolean cacheLoadingNow;
    private boolean netLoadingNow;

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(netLoadingNow);
        }
    }

    private void loadCachedData() {
        this.cacheLoadingNow = true;

        final int accoutnId = super.getAccountId();
        cacheDisposable.add(faveInteractor.getCachedVideos(accoutnId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        this.cacheLoadingNow = false;
        showError(getView(), t);
    }

    private void onCachedDataReceived(List<Video> videos) {
        this.cacheLoadingNow = false;

        this.mVideos.clear();
        this.mVideos.addAll(videos);
        callView(IFaveVideosView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    private void request(int offset) {
        this.netLoadingNow = true;
        resolveRefreshingView();

        final int accountId = super.getAccountId();

        netDisposable.add(faveInteractor.getVideos(accountId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> onNetDataReceived(offset, videos), this::onNetDataGetError));
    }

    private void onNetDataGetError(Throwable t) {
        this.netLoadingNow = false;
        resolveRefreshingView();
        showError(getView(), t);
    }

    private void onNetDataReceived(int offset, List<Video> videos) {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.mEndOfContent = videos.isEmpty();
        this.netLoadingNow = false;

        if (offset == 0) {
            mVideos.clear();
            mVideos.addAll(videos);
            callView(IFaveVideosView::notifyDataSetChanged);
        } else {
            int startSize = mVideos.size();
            mVideos.addAll(videos);
            callView(view -> view.notifyDataAdded(startSize, videos.size()));
        }

        resolveRefreshingView();
    }

    private void requestAtLast() {
        request(0);
    }

    private void requestNext() {
        request(mVideos.size());
    }

    @Override
    public void onGuiCreated(@NonNull IFaveVideosView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(mVideos);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private boolean canLoadMore() {
        return !mVideos.isEmpty() && !cacheLoadingNow && !netLoadingNow && !mEndOfContent;
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.netDisposable.clear();
        this.netLoadingNow = false;
        this.netLoadingNow = false;

        requestAtLast();
    }

    public void fireVideoClick(Video video) {
        getView().goToPreview(getAccountId(), video);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }
}
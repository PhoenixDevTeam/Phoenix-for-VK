package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.ICommunitiesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.DataWrapper;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICommunitiesView;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesPresenter extends AccountDependencyPresenter<ICommunitiesView> {

    private final int userId;

    private final DataWrapper<Community> own;
    private final DataWrapper<Community> filtered;
    private final DataWrapper<Community> search;

    private final ICommunitiesInteractor communitiesInteractor;

    public CommunitiesPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        this.communitiesInteractor = InteractorFactory.createCommunitiesInteractor();

        this.own = new DataWrapper<>(new ArrayList<>(), true);
        this.filtered = new DataWrapper<>(new ArrayList<>(0), false);
        this.search = new DataWrapper<>(new ArrayList<>(0), false);

        loadCachedData();
        requestActualData(0);
    }

    private CompositeDisposable actualDisposable = new CompositeDisposable();
    private boolean actualLoadingNow;
    private int actualLoadingOffset;

    private void requestActualData(int offset) {
        this.actualLoadingNow = true;
        this.actualLoadingOffset = offset;

        final int accountId = super.getAccountId();

        resolveRefreshing();
        actualDisposable.add(communitiesInteractor.getActual(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(communities -> onActualDataReceived(offset, communities), this::onActualDataGetError));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshing();
    }

    private void resolveRefreshing(){
        if(isGuiResumed()){
            getView().displayRefreshing(actualLoadingNow);
        }
    }

    private void onActualDataGetError(Throwable t) {
        this.actualLoadingNow = false;

        resolveRefreshing();
        showError(getView(), t);
    }

    @Override
    public void onGuiCreated(@NonNull ICommunitiesView view) {
        super.onGuiCreated(view);
        view.displayData(own, filtered, search);
    }

    private void onActualDataReceived(int offset, List<Community> communities) {
        Logger.d(tag(), "onActualDataReceived, count: " + communities.size());

        //reset cache loading
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualLoadingNow = false;

        if (offset == 0) {
            this.own.get().clear();
            this.own.get().addAll(communities);
            callView(ICommunitiesView::notifyDataSetChanged);
        } else {
            int startOwnSize = this.own.size();
            this.own.get().addAll(communities);
            callView(view -> view.notifyOwnDataAdded(startOwnSize, communities.size()));
        }

        resolveRefreshing();
    }

    private CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean cacheLoadingNow;

    private void loadCachedData() {
        this.cacheLoadingNow = true;

        final int accountId = super.getAccountId();
        cacheDisposable.add(communitiesInteractor.getCachedData(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived));
    }

    private void onCachedDataReceived(List<Community> communities) {
        this.cacheLoadingNow = false;

        this.own.get().clear();
        this.own.get().addAll(communities);
        callView(ICommunitiesView::notifyDataSetChanged);
    }

    @Override
    protected String tag() {
        return CommunitiesPresenter.class.getSimpleName();
    }

    public void fireSearchQueryChanged(String query) {

    }

    public void fireCommunityClick(Community community) {
        getView().showCommunityWall(getAccountId(), community);
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDisposable.clear();
        actualLoadingNow = false;
        actualLoadingOffset = 0;

        requestActualData(0);
    }
}
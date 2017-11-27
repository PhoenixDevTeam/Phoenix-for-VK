package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.domain.ICommunitiesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.DataWrapper;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICommunitiesView;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Translit;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.trimmedIsEmpty;
import static biz.dealnote.messenger.util.Utils.trimmedNonEmpty;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesPresenter extends AccountDependencyPresenter<ICommunitiesView> {

    private final int userId;

    private final DataWrapper<Community> own;
    private final DataWrapper<Community> filtered;
    private final DataWrapper<Community> search;

    private boolean actualEndOfContent;
    private boolean netSearchEndOfContent;

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
    //private int actualLoadingOffset;

    private void requestActualData(int offset) {
        this.actualLoadingNow = true;
        //this.actualLoadingOffset = offset;

        final int accountId = super.getAccountId();

        resolveRefreshing();
        actualDisposable.add(communitiesInteractor.getActual(accountId, userId, 1000, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(communities -> onActualDataReceived(offset, communities), this::onActualDataGetError));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshing();
    }

    private void resolveRefreshing() {
        if (isGuiResumed()) {
            getView().displayRefreshing(actualLoadingNow || netSeacrhNow);
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
        this.actualEndOfContent = communities.isEmpty();

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

    private CompositeDisposable netSeacrhDisposable = new CompositeDisposable();
    private boolean netSeacrhNow;
    //private int netSearchOffset;

    private boolean isSearchNow() {
        return trimmedNonEmpty(filter);
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

    private String filter;

    public void fireSearchQueryChanged(String query) {
        if (!Objects.safeEquals(filter, query)) {
            this.filter = query;
            onFilterChanged();
        }
    }

    private void onFilterChanged() {
        boolean searchNow = Utils.trimmedNonEmpty(this.filter);

        own.setEnabled(!searchNow);

        filtered.setEnabled(searchNow);
        filtered.clear();

        search.setEnabled(searchNow);
        search.clear();

        callView(ICommunitiesView::notifyDataSetChanged);

        filterDisposable.clear();
        netSeacrhDisposable.clear();
        //netSearchOffset = 0;
        netSeacrhNow = false;

        if (searchNow) {
            filterDisposable.add(filter(own.get(), filter)
                    .compose(RxUtils.applySingleComputationToMainSchedulers())
                    .subscribe(this::onFilteredDataReceived, t -> {/*ignored*/}));

            startNetSearch(0, true);
        } else {
            resolveRefreshing();
        }
    }

    private void startNetSearch(int offset, boolean withDelay) {
        final int accountId = super.getAccountId();
        final String filter = this.filter;

        Single<List<Community>> single;
        Single<List<Community>> searchSingle = communitiesInteractor.search(accountId, filter, null,
                null, null, null, 0, 100, offset);

        if (withDelay) {
            single = Completable.complete()
                    .delay(1, TimeUnit.SECONDS)
                    .andThen(searchSingle);
        } else {
            single = searchSingle;
        }

        this.netSeacrhNow = true;
        //this.netSearchOffset = offset;

        resolveRefreshing();
        netSeacrhDisposable.add(single
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onSearchDataReceived(offset, data), this::onSeacrhError));
    }

    private void onSeacrhError(Throwable t){
        this.netSeacrhNow = false;
        resolveRefreshing();
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onSearchDataReceived(int offset, List<Community> communities){
        this.netSeacrhNow = false;
        this.netSearchEndOfContent = communities.isEmpty();

        resolveRefreshing();

        if(offset == 0){
            this.search.replace(communities);
            callView(ICommunitiesView::notifyDataSetChanged);
        } else {
            int sizeBefore = this.search.size();
            int count = communities.size();

            this.search.addAll(communities);
            callView(view -> view.notifySeacrhDataAdded(sizeBefore, count));
        }
    }

    private void onFilteredDataReceived(List<Community> filteredData) {
        this.filtered.replace(filteredData);
        callView(ICommunitiesView::notifyDataSetChanged);
    }

    private CompositeDisposable filterDisposable = new CompositeDisposable();

    private static Single<List<Community>> filter(final List<Community> orig, final String filter) {
        return Single.create(emitter -> {
            List<Community> result = new ArrayList<>(5);

            for (Community community : orig) {
                if (emitter.isDisposed()) {
                    break;
                }

                if (isMatchFilter(community, filter)) {
                    result.add(community);
                }
            }

            emitter.onSuccess(result);
        });
    }

    private static boolean isMatchFilter(Community community, String filter) {
        if (trimmedIsEmpty(filter)) {
            return true;
        }

        String lower = filter.toLowerCase().trim();

        if (nonEmpty(community.getName())) {
            String lowername = community.getName().toLowerCase();
            if (lowername.contains(lower)) {
                return true;
            }

            try {
                if (lowername.contains(Translit.cyr2lat(lower))) {
                    return true;
                }
            } catch (Exception ignored) {
            }


            try {
                //Caused by java.lang.StringIndexOutOfBoundsException: length=3; index=3
                if (lowername.contains(Translit.lat2cyr(lower))) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return nonEmpty(community.getScreenName()) && community.getScreenName().toLowerCase().contains(lower);
    }

    public void fireCommunityClick(Community community) {
        getView().showCommunityWall(getAccountId(), community);
    }

    @Override
    public void onDestroyed() {
        actualDisposable.dispose();
        cacheDisposable.dispose();
        filterDisposable.dispose();
        netSeacrhDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        if(isSearchNow()){
            netSeacrhDisposable.clear();
            netSeacrhNow = false;

            startNetSearch(0, false);
        } else {
            cacheDisposable.clear();
            cacheLoadingNow = false;

            actualDisposable.clear();
            actualLoadingNow = false;
            //actualLoadingOffset = 0;

            requestActualData(0);
        }
    }

    public void fireScrollToEnd() {
        if(isSearchNow()){
            if(!netSeacrhNow && !netSearchEndOfContent){
                int offset = search.size();
                startNetSearch(offset, false);
            }
        } else {
            if(!actualLoadingNow && !cacheLoadingNow && !actualEndOfContent){
                int offset = own.size();
                requestActualData(offset);
            }
        }
    }
}
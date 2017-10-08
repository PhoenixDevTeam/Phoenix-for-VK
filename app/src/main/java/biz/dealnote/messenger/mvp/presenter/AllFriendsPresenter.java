package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IRelationshipInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UsersPart;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IAllFriendsView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.trimmedIsEmpty;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class AllFriendsPresenter extends AccountDependencyPresenter<IAllFriendsView> {

    private static final int ALL = 0;
    private static final int SEACRH_CACHE = 1;
    private static final int SEARCH_WEB = 2;

    private static final int WEB_SEARCH_DELAY = 1000;
    private static final int WEB_SEARCH_COUNT_PER_LOAD = 100;

    private final IRelationshipInteractor relationshipInteractor;
    private final int userId;

    private ArrayList<UsersPart> data;
    private String q;

    private boolean actualDataReceived;
    private boolean actualDataEndOfContent;

    public AllFriendsPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        this.relationshipInteractor = InteractorFactory.createRelationshipInteractor();

        this.data = new ArrayList<>(3);
        this.data.add(ALL, new UsersPart(R.string.all_friends, new ArrayList<>(), true));
        this.data.add(SEACRH_CACHE, new UsersPart(R.string.results_in_the_cache, new ArrayList<>(), false));
        this.data.add(SEARCH_WEB, new UsersPart(R.string.results_in_a_network, new ArrayList<>(), false));

        loadAllCachedData();
        requestActualData(0);
    }

    private boolean actualDataLoadingNow;

    private CompositeDisposable actualDataDisposable = new CompositeDisposable();

    private void requestActualData(int offset) {
        this.actualDataLoadingNow = true;
        resolveRefreshingView();

        final int accountId = super.getAccountId();

        actualDataDisposable.add(relationshipInteractor.getActualFriendsList(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onActualDataReceived(offset, users), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        this.actualDataLoadingNow = false;
        resolveRefreshingView();
        showError(getView(), getCauseIfRuntime(t));
    }

    @Override
    public void onGuiCreated(@NonNull IAllFriendsView view) {
        super.onGuiCreated(view);
        view.displayData(this.data, isSeacrhNow());
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(!isSeacrhNow() && actualDataLoadingNow);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<User> users) {
        // reset cache loading
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualDataEndOfContent = users.isEmpty();
        this.actualDataReceived = true;
        this.actualDataLoadingNow = false;

        if (offset > 0) {
            int startSize = getAllData().size();
            getAllData().addAll(users);

            if (!isSeacrhNow()) {
                callView(view -> view.notifyItemRangeInserted(startSize, users.size()));
            }
        } else {
            getAllData().clear();
            getAllData().addAll(users);

            if (!isSeacrhNow()) {
                safelyNotifyDataSetChanged();
            }
        }

        resolveRefreshingView();
    }

    private boolean cacheLoadingNow;
    private CompositeDisposable cacheDisposable = new CompositeDisposable();

    private void loadAllCachedData() {
        final int accountId = super.getAccountId();

        this.cacheLoadingNow = true;
        cacheDisposable.add(relationshipInteractor.getCachedFriends(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheGetError));
    }

    private void onCacheGetError(Throwable t) {
        this.cacheLoadingNow = false;
        showError(getView(), t);
    }

    private void onCachedDataReceived(List<User> users) {
        this.cacheLoadingNow = false;

        getAllData().clear();
        getAllData().addAll(users);

        safelyNotifyDataSetChanged();
    }

    private void safelyNotifyDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyDatasetChanged(isSeacrhNow());
        }
    }

    private List<User> getAllData() {
        return this.data.get(ALL).users;
    }

    @Override
    protected String tag() {
        return AllFriendsPresenter.class.getSimpleName();
    }

    public void fireRefresh() {
        if (!isSeacrhNow()) {
            this.cacheDisposable.clear();
            this.actualDataDisposable.clear();
            this.cacheLoadingNow = false;
            this.actualDataLoadingNow = false;

            requestActualData(0);
        }
    }

    private void onSearchQueryChanged(boolean seacrhStateChanged) {
        this.seacrhDisposable.clear();

        if (seacrhStateChanged) {
            resolveSwipeRefreshAvailability();
        }

        if (!isSeacrhNow()) {
            data.get(ALL).enable = true;

            data.get(SEARCH_WEB).users.clear();
            data.get(SEARCH_WEB).enable = false;
            data.get(SEARCH_WEB).displayCount = null;

            data.get(SEACRH_CACHE).users.clear();
            data.get(SEACRH_CACHE).enable = false;

            callView(view -> view.notifyDatasetChanged(false));
            return;
        }

        data.get(ALL).enable = false;

        reFillCache();
        data.get(SEACRH_CACHE).enable = true;

        data.get(SEARCH_WEB).users.clear();
        data.get(SEARCH_WEB).enable = true;
        data.get(SEARCH_WEB).displayCount = null;

        callView(view -> view.notifyDatasetChanged(true));

        runNetSeacrh(0, true);
    }

    private boolean searchRunNow;

    private void runNetSeacrh(int offset, boolean withDelay) {
        if (trimmedIsEmpty(this.q)) {
            return;
        }

        this.seacrhDisposable.clear();
        this.searchRunNow = true;

        final String query = this.q;
        final int accountId = super.getAccountId();

        final Single<Pair<List<User>, Integer>> single;
        Single<Pair<List<User>, Integer>> netSingle = relationshipInteractor.seacrhFriends(accountId, userId, WEB_SEARCH_COUNT_PER_LOAD, offset, query);

        if (withDelay) {
            single = Single.just(new Object())
                    .delay(WEB_SEARCH_DELAY, TimeUnit.MILLISECONDS)
                    .flatMap(ignored -> netSingle);
        } else {
            single = netSingle;
        }

        seacrhDisposable.add(single
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onSearchDataReceived(offset, pair.getFirst(), pair.getSecond()), this::onSearchError));
    }

    private void onSearchError(Throwable t) {
        this.searchRunNow = false;
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onSearchDataReceived(int offset, List<User> users, int fullCount) {
        this.searchRunNow = false;

        List<User> searchData = data.get(SEARCH_WEB).users;

        data.get(SEARCH_WEB).displayCount = fullCount;

        if (offset == 0) {
            searchData.clear();
            searchData.addAll(users);
            callView(view -> view.notifyDatasetChanged(isSeacrhNow()));
        } else {
            int sizeBefore = searchData.size();
            int currentCacheSize = data.get(SEACRH_CACHE).users.size();
            searchData.addAll(users);
            callView(view -> view.notifyItemRangeInserted(sizeBefore + currentCacheSize, users.size()));
        }
    }

    private static boolean allow(User user, String preparedQ) {
        String full = user.getFullName().toLowerCase();
        return full.contains(preparedQ);
    }

    private void reFillCache() {
        data.get(SEACRH_CACHE).users.clear();

        List<User> db = data.get(ALL).users;

        String preparedQ = this.q.toLowerCase().trim();

        int count = 0;
        for (User user : db) {
            if (allow(user, preparedQ)) {
                data.get(SEACRH_CACHE).users.add(user);
                count++;
            }
        }

        data.get(SEACRH_CACHE).displayCount = count;
    }

    private CompositeDisposable seacrhDisposable = new CompositeDisposable();

    private boolean isSeacrhNow() {
        return nonEmpty(q);
    }

    @OnGuiCreated
    private void resolveSwipeRefreshAvailability() {
        if (isGuiReady()) {
            getView().setSwipeRefreshEnabled(!isSeacrhNow());
        }
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();

        if (Objects.safeEquals(q, this.q)) {
            return;
        }

        boolean wasSearch = isSeacrhNow();
        this.q = query;

        onSearchQueryChanged(wasSearch != isSeacrhNow());
    }

    @Override
    public void onDestroyed() {
        seacrhDisposable.dispose();
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    private void loadMore() {
        if (isSeacrhNow()) {
            if (this.searchRunNow) {
                return;
            }

            runNetSeacrh(this.data.get(SEARCH_WEB).users.size(), false);
        } else {
            if (this.actualDataLoadingNow || this.cacheLoadingNow || !actualDataReceived || actualDataEndOfContent) {
                return;
            }

            requestActualData(getAllData().size());
        }
    }

    public void fireScrollToEnd() {
        this.loadMore();
    }

    public void fireUserClick(User user) {
        getView().showUserWall(getAccountId(), user);
    }
}
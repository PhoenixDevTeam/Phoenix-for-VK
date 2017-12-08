package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.AbsNextFrom;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.search.IBaseSearchView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.WeakActionHandler;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public abstract class AbsSearchPresenter<V extends IBaseSearchView<T>, C extends BaseSearchCriteria, T, N extends AbsNextFrom> extends PlaceSupportPresenter<V> {

    private static final String SAVE_CRITERIA = "save_criteria";
    private static final int MESSAGE = 67;
    private static final int SEARCH_DELAY = 1500;

    final List<T> data;
    private final C criteria;
    private N nextFrom;

    private C resultsForCriteria;
    private boolean endOfContent;

    private WeakActionHandler<AbsSearchPresenter> actionHandler = new WeakActionHandler<>(this);

    AbsSearchPresenter(int accountId, @Nullable C criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        if (isNull(savedInstanceState)) {
            this.criteria = isNull(criteria) ? instantiateEmptyCriteria() : criteria;
        } else {
            this.criteria = savedInstanceState.getParcelable(SAVE_CRITERIA);
        }

        this.nextFrom = getInitialNextFrom();
        this.data = new ArrayList<>();
        this.actionHandler.setAction((what, object) -> object.doSearch());
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        // пробуем искать при первом создании view
        if(super.getViewCreationCount() == 1){
            doSearch();
        }
    }

    C getCriteria() {
        return criteria;
    }

    abstract N getInitialNextFrom();

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CRITERIA, criteria);
    }

    private CompositeDisposable searchDisposable = new CompositeDisposable();

    @SuppressWarnings("unchecked")
    void doSearch() {
        if(!canSearch(this.criteria) || isNull(this.nextFrom)){
            //setLoadingNow(false);
            return;
        }

        final int accountId = getAccountId();
        final C cloneCriteria = (C) criteria.safellyClone();
        final N nf = this.nextFrom;

        setLoadingNow(true);
        searchDisposable.add(doSearch(accountId, cloneCriteria, nf)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onSearchDataReceived(cloneCriteria, nf, pair.getFirst(), pair.getSecond()),
                        this::onSeacrhError));
    }

    private void onSeacrhError(Throwable throwable) {
        throwable.printStackTrace();
    }

    abstract boolean isAtLast(N startFrom);

    private void onSearchDataReceived(C criteria, N startFrom, List<T> data, N nextFrom) {
        setLoadingNow(false);

        boolean clearPrevious = isAtLast(startFrom);

        this.nextFrom = nextFrom;
        this.resultsForCriteria = criteria;
        this.endOfContent = data.isEmpty();

        if (clearPrevious) {
            this.data.clear();
            this.data.addAll(data);
            callView(IBaseSearchView::notifyDataSetChanged);
        } else {
            int startSize = this.data.size();
            this.data.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveEmptyText();
    }

    public final void fireTextQueryEdit(String q) {
        criteria.setQuery(q);

        fireCriteriaChanged();
    }

    @OnGuiCreated
    private void resolveListData() {
        if (isGuiReady()) {
            getView().displayData(data);
        }
    }

    @OnGuiCreated
    private void resolveEmptyText() {
        if (isGuiReady()) {
            getView().setEmptyTextVisible(data.isEmpty());
        }
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView(){
        if(isGuiResumed()){
            getView().showLoading(loadingNow);
        }
    }

    private void fireCriteriaChanged() {
        if (criteria.equals(resultsForCriteria)) {
            return;
        }

        searchDisposable.clear();
        setLoadingNow(false);

        this.nextFrom = getInitialNextFrom();
        this.data.clear();

        resolveListData();
        resolveEmptyText();

        actionHandler.removeMessages(MESSAGE);

        if(canSearch(criteria)){
            actionHandler.sendEmptyMessageDelayed(MESSAGE, SEARCH_DELAY);
        }
    }

    abstract Single<Pair<List<T>, N>> doSearch(int accountId, C criteria, N startFrom);

    abstract C instantiateEmptyCriteria();

    @Override
    public void onDestroyed() {
        this.actionHandler.setAction(null);
        this.searchDisposable.clear();
        super.onDestroyed();
    }

    abstract boolean canSearch(C criteria);

    public final void fireScrollToEnd() {
        if(canLoadMore()){
            doSearch();
        }
    }

    private boolean canLoadMore(){
        return !endOfContent && !loadingNow && !data.isEmpty() && nonNull(nextFrom);
    }

    public void fireRefresh() {
        if(loadingNow || !canSearch(criteria)){
            resolveLoadingView();
            return;
        }

        this.nextFrom = getInitialNextFrom();
        doSearch();
    }

    public void fireSyncCriteriaRequest() {
        getView().displaySearchQuery(criteria.getQuery());
    }

    public void fireOptionsChanged() {
        fireCriteriaChanged();
    }

    public void fireOpenFilterClick() {
        getView().displayFilter(getAccountId(), criteria.getOptions());
    }
}
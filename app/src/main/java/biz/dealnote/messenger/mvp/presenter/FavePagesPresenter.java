package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.IFaveInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.EndlessData;
import biz.dealnote.messenger.model.FavePage;
import biz.dealnote.messenger.model.FavePageType;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IFaveUsersView;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.findIndexById;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public class FavePagesPresenter extends AccountDependencyPresenter<IFaveUsersView> {

    private final List<FavePage> pages;

    @FavePageType
    private final String type;

    private final IFaveInteractor faveInteractor;

    private boolean actualDataReceived;

    private boolean endOfContent;

    public FavePagesPresenter(int accountId, @FavePageType String type, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.pages = new ArrayList<>();
        this.type = type;
        this.faveInteractor = InteractorFactory.createFaveInteractor();

        loadAllCachedData();
        loadActualData(0);
    }

    @Override
    public void onGuiCreated(@NonNull IFaveUsersView view) {
        super.onGuiCreated(view);
        view.displayData(this.pages);
    }

    private boolean cacheLoadingNow;
    private CompositeDisposable cacheDisposable = new CompositeDisposable();

    private boolean actualDataLoading;
    private CompositeDisposable actualDataDisposable = new CompositeDisposable();

    private void loadActualData(int offset) {
        this.actualDataLoading = true;

        resolveRefreshingView();

        final int accountId = super.getAccountId();
        switch (type) {
            case FavePageType.USER:
                actualDataDisposable.add(faveInteractor.getUsers(accountId, 50, offset)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));
                break;
            case FavePageType.COMMUNITY:
                actualDataDisposable.add(faveInteractor.getGroups(accountId, 50, offset)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));
                break;
        }

    }

    private void onActualDataGetError(Throwable t) {
        this.actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, EndlessData<FavePage> data) {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualDataLoading = false;
        this.endOfContent = !data.hasNext();
        this.actualDataReceived = true;

        if (offset == 0) {
            this.pages.clear();
            this.pages.addAll(data.get());
            callView(IFaveUsersView::notifyDataSetChanged);
        } else {
            int startSize = this.pages.size();
            this.pages.addAll(data.get());
            callView(view -> view.notifyDataAdded(startSize, data.get().size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(actualDataLoading);
        }
    }

    private void loadAllCachedData() {
        this.cacheLoadingNow = true;
        final int accountId = super.getAccountId();
        switch (type) {
            case FavePageType.USER:
                cacheDisposable.add(faveInteractor.getCachedUsers(accountId)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::onCachedDataReceived, this::onCachedGetError));
                break;
            case FavePageType.COMMUNITY:
                cacheDisposable.add(faveInteractor.getCachedGroups(accountId)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::onCachedDataReceived, this::onCachedGetError));
                break;
        }

    }

    private void onCachedGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onCachedDataReceived(List<FavePage> data) {
        this.cacheLoadingNow = false;

        this.pages.clear();
        this.pages.addAll(data);
        callView(IFaveUsersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public void fireScrollToEnd() {
        if (!endOfContent && nonEmpty(pages) && actualDataReceived && !cacheLoadingNow && !actualDataLoading) {
            loadActualData(this.pages.size());
        }
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualDataDisposable.clear();
        this.actualDataLoading = false;

        loadActualData(0);
    }

    public void fireOwnerClick(Owner owner) {
        getView().openOwnerWall(getAccountId(), owner);
    }

    private void onUserRemoved(int accountId, int userId) {
        if (getAccountId() != accountId) {
            return;
        }

        int index = findIndexById(this.pages, userId);

        if (index != -1) {
            this.pages.remove(index);
            callView(view -> view.notifyItemRemoved(index));
        }
    }

    public void fireOwnerDelete(Owner owner) {
        final int accountId = super.getAccountId();
        final int userId = owner.getOwnerId();
        appendDisposable(faveInteractor.removeUser(accountId, userId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUserRemoved(accountId, userId), t -> showError(getView(), getCauseIfRuntime(t))));
    }
}
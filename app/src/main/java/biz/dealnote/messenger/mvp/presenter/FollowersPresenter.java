package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IRelationshipInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class FollowersPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final int userId;
    private final IRelationshipInteractor relationshipInteractor;

    public FollowersPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        this.relationshipInteractor = InteractorFactory.createRelationshipInteractor();

        loadAllCacheData();
        requestActualData(0);
    }

    private boolean actualDataLoading;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private CompositeDisposable actualDataDisposable = new CompositeDisposable();

    private void requestActualData(int offset) {
        this.actualDataLoading = true;
        resolveRefreshingView();

        final int accountId = super.getAccountId();
        actualDataDisposable.add(relationshipInteractor.getFollowers(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onActualDataReceived(offset, users), this::onActualDataGetError));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayRefreshing(actualDataLoading);
        }
    }

    private void onActualDataGetError(Throwable t) {
        this.actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<User> users) {
        this.actualDataLoading = false;

        this.cacheDisposable.clear();
        this.actualDataLoading = false;

        this.actualDataReceived = true;
        this.endOfContent = users.isEmpty();

        if (offset == 0) {
            super.data.clear();
            super.data.addAll(users);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int startSzie = super.data.size();
            super.data.addAll(users);
            callView(view -> view.notifyDataAdded(startSzie, users.size()));
        }

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !cacheLoadingNow && !actualDataLoading && actualDataReceived) {
            requestActualData(super.data.size());
        }
    }

    @Override
    void onUserRefreshed() {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualDataDisposable.clear();
        requestActualData(0);
    }

    private boolean cacheLoadingNow;
    private CompositeDisposable cacheDisposable = new CompositeDisposable();

    private void loadAllCacheData() {
        this.cacheLoadingNow = true;

        final int accountId = super.getAccountId();
        cacheDisposable.add(relationshipInteractor.getCachedFollowers(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheDataGetError));
    }

    private void onCacheDataGetError(Throwable t) {
        this.cacheLoadingNow = false;
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onCachedDataReceived(List<User> users) {
        this.cacheLoadingNow = false;

        super.data.addAll(users);
        callView(ISimpleOwnersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    protected String tag() {
        return FollowersPresenter.class.getSimpleName();
    }
}
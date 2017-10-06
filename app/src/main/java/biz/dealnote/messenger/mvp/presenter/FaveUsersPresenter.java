package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.interactor.IFaveInteractor;
import biz.dealnote.messenger.interactor.InteractorFactory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IFaveUsersView;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public class FaveUsersPresenter extends AccountDependencyPresenter<IFaveUsersView> {

    private final List<User> users;

    private final IFaveInteractor faveInteractor;

    private boolean actualDataReceived;

    private boolean endOfContent;

    public FaveUsersPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.users = new ArrayList<>();
        this.faveInteractor = InteractorFactory.createFaveInteractor();

        loadAllCachedData();
    }

    private boolean cacheLoadingNow;
    private CompositeDisposable cacheDisposable = new CompositeDisposable();

    private boolean actualDataLoading;
    private CompositeDisposable actualDataDisposable = new CompositeDisposable();

    private void loadActualData(int offset) {
        this.actualDataLoading = true;

        resolveRefreshingView();

        final int accountId = super.getAccountId();
        actualDataDisposable.add(faveInteractor.getUsers(accountId, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        this.actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, List<User> data) {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;

        this.actualDataLoading = false;
        this.endOfContent = data.isEmpty();
        this.actualDataReceived = true;

        if(offset == 0){
            this.users.clear();
            this.users.addAll(data);
            callView(IFaveUsersView::notifyDataSetChanged);
        } else {
            int startSize = this.users.size();
            this.users.addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView(){
        if(isGuiResumed()){
            getView().showRefreshing(actualDataLoading);
        }
    }

    private void loadAllCachedData() {
        this.cacheLoadingNow = true;
        final int accountId = super.getAccountId();
        cacheDisposable.add(faveInteractor.getCachedUsers(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCachedGetError));
    }

    private void onCachedGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private void onCachedDataReceived(List<User> data) {
        this.cacheLoadingNow = false;

        this.users.clear();
        this.users.addAll(data);
        callView(IFaveUsersView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    protected String tag() {
        return FaveUsersPresenter.class.getSimpleName();
    }

    public void fireScrollToEnd() {
        if(!endOfContent && nonEmpty(users) && actualDataReceived && !cacheLoadingNow && !actualDataLoading){
            loadActualData(this.users.size());
        }
    }

    public void fireRefresh() {
        this.cacheDisposable.clear();
        this.cacheLoadingNow = false;
        this.actualDataDisposable.clear();
        this.actualDataLoading = false;

        loadActualData(0);
    }

    public void fireUserClick(User user) {
        getView().openUserWall(getAccountId(), user);
    }

    public void fireUserDelete(User user) {
        // TODO: 11.09.2017
    }
}
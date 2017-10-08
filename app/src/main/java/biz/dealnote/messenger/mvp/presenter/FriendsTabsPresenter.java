package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.IRelationshipInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IFriendsTabsView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public class FriendsTabsPresenter extends AccountDependencyPresenter<IFriendsTabsView> {

    private static final String SAVE_COUNTERS = "save_counters";

    private final int userId;

    private FriendsCounters counters;

    private final IRelationshipInteractor relationshipInteractor;
    private final IOwnersInteractor ownersInteractor;

    private Owner owner;

    public FriendsTabsPresenter(int accountId, int userId, @Nullable FriendsCounters counters, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        this.relationshipInteractor = InteractorFactory.createRelationshipInteractor();
        this.ownersInteractor = InteractorFactory.createOwnerInteractor();

        if (Objects.nonNull(savedInstanceState)) {
            this.counters = savedInstanceState.getParcelable(SAVE_COUNTERS);
        } else {
            this.counters = counters;
        }

        if (this.counters == null) {
            this.counters = new FriendsCounters(0, 0, 0, 0);
            requestCounters();
        }

        if (Objects.isNull(owner) && userId != accountId) {
            requestOwnerInfo();
        }
    }

    private void requestOwnerInfo() {
        final int accountId = super.getAccountId();
        appendDisposable(ownersInteractor.getBaseOwnerInfo(accountId, userId, IOwnersInteractor.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onOwnerInfoReceived, t -> {/*ignore*/}));
    }

    private void onOwnerInfoReceived(Owner owner) {
        this.owner = owner;
        callView(view -> view.displayUserNameAtToolbar(owner.getFullName()));
    }

    private void requestCounters() {
        final int accountId = super.getAccountId();
        appendDisposable(relationshipInteractor.getFriendsCounters(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCountersReceived, this::onCountersGetError));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        getView().setDrawerFriendsSectionSelected(this.userId == super.getAccountId());
    }

    private void onCountersGetError(Throwable t) {
        showError(getView(), t);
    }

    private void onCountersReceived(FriendsCounters counters) {
        this.counters = counters;
        callView(view -> view.displayConters(counters));
    }

    @Override
    public void onGuiCreated(@NonNull IFriendsTabsView view) {
        super.onGuiCreated(view);
        view.configTabs(getAccountId(), userId, userId != getAccountId());
        view.displayConters(this.counters);
    }

    @Override
    protected String tag() {
        return FriendsTabsPresenter.class.getSimpleName();
    }
}
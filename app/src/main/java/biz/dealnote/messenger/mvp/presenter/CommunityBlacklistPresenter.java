package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.db.interfaces.IOwnersStore;
import biz.dealnote.messenger.db.model.BanAction;
import biz.dealnote.messenger.domain.IGroupSettingsInteractor;
import biz.dealnote.messenger.domain.impl.GroupSettingsInteractor;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityBlacklistView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityBlacklistPresenter extends AccountDependencyPresenter<ICommunityBlacklistView> {

    private static final int COUNT = 20;
    private final String TAG = CommunityBlacklistPresenter.class.getSimpleName();

    private final int groupId;
    private final List<Banned> data;

    private final IGroupSettingsInteractor groupSettingsInteractor;

    private boolean loadingNow;

    private IntNextFrom moreStartFrom;
    private boolean endOfContent;

    public CommunityBlacklistPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        this.data = new ArrayList<>();
        this.moreStartFrom = new IntNextFrom(0);

        INetworker networker = Injection.provideNetworkInterfaces();
        IOwnersStore repository = Injection.provideStores().owners();

        this.groupSettingsInteractor = new GroupSettingsInteractor(networker, repository);

        appendDisposable(repository.observeBanActions()
                .filter(action -> action.getGroupId() == groupId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onBanActionReceived));

        requestDataAtStart();
    }

    private void onBanActionReceived(BanAction action) {
        if (action.isBan()) {
            //refresh data
            requestDataAtStart();
        } else {
            int index = Utils.findIndexByPredicate(data, banned -> banned.getUser().getId() == action.getUserId());
            if (index != -1) {
                data.remove(index);
                callView(view -> view.notifyItemRemoved(index));
            }
        }
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    private void requestDataAtStart(){
        request(new IntNextFrom(0));
    }

    private void request(IntNextFrom startFrom){
        if (loadingNow) return;

        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(groupSettingsInteractor.getBanned(accountId, groupId, startFrom, COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onBannedUsersReceived(startFrom, pair.getSecond(), pair.getFirst()),
                        throwable -> onRequqestError(getCauseIfRuntime(throwable))));
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityBlacklistView view) {
        super.onGuiCreated(view);
        view.diplayData(this.data);
    }

    private void onRequqestError(Throwable throwable) {
        setLoadingNow(false);

        throwable.printStackTrace();
        showError(getView(), throwable);
    }

    private void onBannedUsersReceived(IntNextFrom startFrom, IntNextFrom nextFrom, List<Banned> users) {
        this.endOfContent = users.isEmpty();
        this.moreStartFrom = nextFrom;

        if(startFrom.getOffset() != 0){
            int startSize = this.data.size();
            this.data.addAll(users);
            callView(view -> view.notifyItemsAdded(startSize, users.size()));
        } else {
            this.data.clear();
            this.data.addAll(users);
            callView(ICommunityBlacklistView::notifyDataSetChanged);
        }

        setLoadingNow(false);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireRefresh() {
        requestDataAtStart();
    }

    public void fireBannedClick(Banned banned) {
        getView().openBanEditor(getAccountId(), groupId, banned);
    }

    public void fireAddClick() {
        getView().startSelectProfilesActivity(getAccountId(), groupId);
    }

    public void fireAddToBanUsersSelected(ArrayList<User> users) {
        if (nonEmpty(users)) {
            getView().addUsersToBan(getAccountId(), groupId, users);
        }
    }

    public void fireBannedRemoveClick(Banned banned) {
        appendDisposable(groupSettingsInteractor
                .unbanUser(getAccountId(), groupId, banned.getUser().getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUnbanComplete(banned),
                        throwable -> onUnbanError(getCauseIfRuntime(throwable))));
    }

    @SuppressWarnings("unused")
    private void onUnbanComplete(Banned banned) {
        safeShowToast(getView(), R.string.deleted, false);
    }

    private void onUnbanError(Throwable throwable) {
        throwable.printStackTrace();
        showError(getView(), throwable);
    }

    private boolean canLoadMore() {
        return !endOfContent && !loadingNow && nonEmpty(data) && moreStartFrom.getOffset() > 0;
    }

    public void fireScrollToBottom() {
        if (canLoadMore()) {
            request(moreStartFrom);
        }
    }
}
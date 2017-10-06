package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityLinksView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.functions.Function;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityLinksPresenter extends AccountDependencyPresenter<ICommunityLinksView> {

    private static final String TAG = CommunityLinksPresenter.class.getSimpleName();

    private final int groupId;

    private List<VKApiCommunity.Link> links;

    private final INetworker networker;

    public CommunityLinksPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.networker = Injection.provideNetworkInterfaces();
        this.groupId = groupId;
        this.links = new ArrayList<>();

        requestLinks();
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityLinksView view) {
        super.onGuiCreated(view);
        view.displayData(links);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    private void requestLinks() {
        final int accountId = super.getAccountId();

        setLoadingNow(true);
        appendDisposable(networker.vkDefault(accountId)
                .groups()
                .getById(Collections.singletonList(groupId), null, null, "links")
                .map((Function<List<VKApiCommunity>, List<VKApiCommunity.Link>>) dtos -> {
                    if (dtos.size() != 1) {
                        throw new NotFoundException();
                    }

                    List<VKApiCommunity.Link> links = dtos.get(0).links;
                    return Objects.nonNull(links) ? links : Collections.emptyList();
                })
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLinksReceived, this::onRequestError));
    }

    private void onRequestError(Throwable throwable) {
        throwable.printStackTrace();

        setLoadingNow(false);
        showError(getView(), throwable);
    }

    private void onLinksReceived(List<VKApiCommunity.Link> links) {
        setLoadingNow(false);

        this.links.clear();
        this.links.addAll(links);

        callView(ICommunityLinksView::notifyDataSetChanged);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireRefresh() {
        requestLinks();
    }

    public void fireLinkClick(VKApiCommunity.Link link) {
        getView().openLink(link.url);
    }

    public void fireLinkEditClick(VKApiCommunity.Link link) {

    }

    public void fireLinkDeleteClick(VKApiCommunity.Link link) {

    }

    public void fireButtonAddClick() {

    }
}
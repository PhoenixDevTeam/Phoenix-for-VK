package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.domain.ICommunitiesInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.CommunityDetails;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.PostFilter;
import biz.dealnote.messenger.model.Token;
import biz.dealnote.messenger.model.criteria.WallCriteria;
import biz.dealnote.messenger.mvp.view.IGroupWallView;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 23.01.2017.
 * phoenix
 */
public class GroupWallPresenter extends AbsWallPresenter<IGroupWallView> {

    private List<PostFilter> filters;

    private final ISettings.IAccountsSettings settings;

    private Community community;

    private CommunityDetails details;

    private final IOwnersInteractor ownersInteractor;

    private final ICommunitiesInteractor communitiesInteractor;

    public GroupWallPresenter(int accountId, int ownerId, @Nullable Community owner, @Nullable Bundle savedInstanceState) {
        super(accountId, ownerId, savedInstanceState);
        this.community = owner;
        this.details = new CommunityDetails();

        if (isNull(this.community)) {
            this.community = new Community(Math.abs(ownerId));
        }

        this.ownersInteractor = InteractorFactory.createOwnerInteractor();
        this.communitiesInteractor = InteractorFactory.createCommunitiesInteractor();
        this.settings = Injection.provideSettings().accounts();

        filters = new ArrayList<>();
        filters.addAll(createPostFilters());

        syncFiltersWithSelectedMode();
        syncFilterCounters();

        refreshInfo();
    }

    @OnGuiCreated
    private void resolveBaseCommunityViews() {
        if (isGuiReady()) {
            getView().displayBaseCommunityData(this.community);
        }
    }

    @OnGuiCreated
    private void resolveCounters() {
        if (isGuiReady()) {
            getView().displayCounters(details.getMembersCount(), details.getTopicsCount(),
                    details.getDocsCount(), details.getPhotosCount(),
                    details.getAudiosCount(), details.getVideosCount());
        }
    }

    private void refreshInfo() {
        final int accountId = super.getAccountId();
        appendDisposable(ownersInteractor.getFullCommunityInfo(accountId, Math.abs(ownerId), IOwnersInteractor.MODE_CACHE)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> {
                    onFullInfoReceived(pair.getFirst(), pair.getSecond());
                    requestActualFullInfo();
                }, t -> {/*ignore*/}));
    }

    private void requestActualFullInfo() {
        final int accountId = super.getAccountId();
        appendDisposable(ownersInteractor.getFullCommunityInfo(accountId, Math.abs(ownerId), IOwnersInteractor.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onFullInfoReceived(pair.getFirst(), pair.getSecond()), this::onDetailsGetError));
    }

    private void onFullInfoReceived(Community community, CommunityDetails details) {
        this.community = community;
        this.details = details;

        filters.clear();
        filters.addAll(createPostFilters());

        syncFiltersWithSelectedMode();
        syncFilterCounters();

        callView(IGroupWallView::notifyWallFiltersChanged);

        resolveActionButtons();
        resolveCounters();
        resolveBaseCommunityViews();
    }

    private void onDetailsGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
    }

    private List<PostFilter> createPostFilters() {
        List<PostFilter> filters = new ArrayList<>();
        filters.add(new PostFilter(WallCriteria.MODE_ALL, getString(R.string.all_posts)));
        filters.add(new PostFilter(WallCriteria.MODE_OWNER, getString(R.string.owner_s_posts)));
        filters.add(new PostFilter(WallCriteria.MODE_SUGGEST, getString(R.string.suggests)));

        if (isAdmin()) {
            filters.add(new PostFilter(WallCriteria.MODE_SCHEDULED, getString(R.string.scheduled)));
        }

        return filters;
    }

    private boolean isAdmin() {
        return community.isAdmin();
    }

    private void syncFiltersWithSelectedMode() {
        for (PostFilter filter : filters) {
            filter.setActive(filter.getMode() == getWallFilter());
        }
    }

    private void syncFilterCounters() {
        for (PostFilter filter : filters) {
            switch (filter.getMode()) {
                case WallCriteria.MODE_ALL:
                    filter.setCount(details.getAllWallCount());
                    break;

                case WallCriteria.MODE_OWNER:
                    filter.setCount(details.getOwnerWallCount());
                    break;

                case WallCriteria.MODE_SCHEDULED:
                    filter.setCount(details.getPostponedWallCount());
                    break;

                case WallCriteria.MODE_SUGGEST:
                    filter.setCount(details.getSuggestedWallCount());
                    break;
            }
        }
    }

    @Override
    public void onGuiCreated(@NonNull IGroupWallView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayWallFilters(filters);
    }

    public void firePrimaryButtonClick() {
        if (community.getMemberStatus() == VKApiCommunity.MemberStatus.IS_MEMBER || community.getMemberStatus() == VKApiCommunity.MemberStatus.SENT_REQUEST) {
            leaveCommunity();
        } else {
            joinCommunity();
        }
    }

    public void fireSecondaryButtonClick() {
        if (community.getMemberStatus() == VKApiCommunity.MemberStatus.INVITED) {
            leaveCommunity();
        }
    }

    private void leaveCommunity() {
        final int accountid = super.getAccountId();
        final int groupId = Math.abs(ownerId);

        appendDisposable(communitiesInteractor.leave(accountid, groupId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onLeaveResult, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    private void joinCommunity() {
        final int accountid = super.getAccountId();
        final int groupId = Math.abs(ownerId);

        appendDisposable(communitiesInteractor.join(accountid, groupId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onJoinResult, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireHeaderPhotosClick() {
        getView().openPhotoAlbums(getAccountId(), ownerId, community);
    }

    public void fireHeaderAudiosClick() {
        getView().openAudios(getAccountId(), ownerId, community);
    }

    public void fireHeaderVideosClick() {
        getView().openVideosLibrary(getAccountId(), ownerId, community);
    }

    public void fireHeaderMembersClick() {
        getView().openCommunityMembers(getAccountId(), Math.abs(ownerId));
    }

    public void fireHeaderTopicsClick() {
        getView().openTopics(getAccountId(), ownerId, community);
    }

    public void fireHeaderDocsClick() {
        getView().openDocuments(getAccountId(), ownerId, community);
    }

    public void fireHeaderStatusClick() {

    }

    @OnGuiCreated
    private void resolveActionButtons() {
        if (!isGuiReady()) return;

        if (community.getType() == VKApiCommunity.Type.EVENT) {
            getView().setupPrimaryButton(null);
            getView().setupSecondaryButton(null);
            return;
        }

        @StringRes
        Integer primaryText = null;
        @StringRes
        Integer secondaryText = null;

        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_NOT_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        switch (community.getClosed()) {
                            case VKApiCommunity.Status.CLOSED:
                                primaryText = R.string.community_send_request;
                                break;
                            case VKApiCommunity.Status.OPEN:
                                primaryText = R.string.community_join;
                                break;
                        }

                        break;
                    case VKApiCommunity.Type.PAGE:
                        primaryText = R.string.community_follow;
                        break;
                }

                break;

            case VKApiCommunity.MemberStatus.IS_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        primaryText = R.string.community_leave;
                        break;
                    case VKApiCommunity.Type.PAGE:
                        primaryText = R.string.community_unsubscribe_from_news;
                        break;
                }

                break;

            case VKApiCommunity.MemberStatus.NOT_SURE:
                // TODO: 07.02.2016 ЧЕРТ РАЗБЕРЕТ ЧТО ЭТО.. скорее всего я не уверен, что пойду на встречу
                break;

            case VKApiCommunity.MemberStatus.DECLINED_INVITATION:
                primaryText = R.string.community_send_request;
                break;

            case VKApiCommunity.MemberStatus.SENT_REQUEST:
                primaryText = R.string.cancel_request;
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                primaryText = R.string.community_join;
                secondaryText = R.string.cancel_invitation;
                break;
        }

        getView().setupPrimaryButton(primaryText);
        getView().setupSecondaryButton(secondaryText);
    }

    private void onLeaveResult() {
        Integer resultMessage = null;
        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_MEMBER:
                community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                community.setMember(false);

                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        resultMessage = R.string.community_leave_success;
                        break;
                    case VKApiCommunity.Type.PAGE:
                        resultMessage = R.string.community_unsubscribe_from_news_success;
                        break;
                }
                break;

            case VKApiCommunity.MemberStatus.SENT_REQUEST:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                    community.setMember(false);
                    resultMessage = R.string.request_canceled;
                }
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                if (community.getType() == VKApiCommunity.Type.GROUP) {
                    community.setMember(false);
                    community.setMemberStatus(VKApiCommunity.MemberStatus.IS_NOT_MEMBER);
                    resultMessage = R.string.invitation_has_been_declined;
                }
                break;
        }

        resolveActionButtons();
        if (nonNull(resultMessage) && isGuiReady()) {
            getView().showSnackbar(resultMessage, true);
        }
    }

    private void onJoinResult() {
        Integer resultMessage = null;

        switch (community.getMemberStatus()) {
            case VKApiCommunity.MemberStatus.IS_NOT_MEMBER:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        switch (community.getClosed()) {
                            case VKApiCommunity.Status.CLOSED:
                                community.setMember(false);
                                community.setMemberStatus(VKApiCommunity.MemberStatus.SENT_REQUEST);
                                resultMessage = R.string.community_send_request_success;
                                break;

                            case VKApiCommunity.Status.OPEN:
                                community.setMember(true);
                                community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                                resultMessage = R.string.community_join_success;
                                break;
                        }
                        break;

                    case VKApiCommunity.Type.PAGE:
                        community.setMember(true);
                        community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                        resultMessage = R.string.community_follow_success;
                        break;
                }
                break;

            case VKApiCommunity.MemberStatus.DECLINED_INVITATION:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        community.setMember(false);
                        community.setMemberStatus(VKApiCommunity.MemberStatus.SENT_REQUEST);
                        resultMessage = R.string.community_send_request_success;
                        break;
                }
                break;

            case VKApiCommunity.MemberStatus.INVITED:
                switch (community.getType()) {
                    case VKApiCommunity.Type.GROUP:
                        community.setMember(true);
                        community.setMemberStatus(VKApiCommunity.MemberStatus.IS_MEMBER);
                        resultMessage = R.string.community_join_success;
                        break;
                }
                break;
        }

        resolveActionButtons();

        if (nonNull(resultMessage) && isGuiReady()) {
            getView().showSnackbar(resultMessage, true);
        }
    }

    public void fireFilterEntryClick(PostFilter entry) {
        if (changeWallFilter(entry.getMode())) {
            syncFiltersWithSelectedMode();

            getView().notifyWallFiltersChanged();
        }
    }

    public void fireCommunityControlClick() {
        getView().goToCommunityControl(getAccountId(), community, null);

        /*final int accountId = super.getAccountId();
        final int grouId = Math.abs(ownerId);

        IGroupSettingsInteractor interactor = new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners());
        appendDisposable(interactor.getGroupSettings(accountId, grouId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onSettingsReceived, throwable -> {
                    showError(getView(), getCauseIfRuntime(throwable));
                }));*/
    }

    //private void onSettingsReceived(GroupSettings settings) {
    //    callView(view -> view.goToCommunityControl(getAccountId(), owner, settings));
    //}

    public void fireCommunityMessagesClick() {
        if (Utils.nonEmpty(settings.getAccessToken(ownerId))) {
            openCommunityMessages();
        } else {
            final int groupId = Math.abs(ownerId);
            getView().startLoginCommunityActivity(groupId);
        }
    }

    private void openCommunityMessages() {
        final int groupId = Math.abs(ownerId);
        final int accountId = super.getAccountId();
        String subtitle = community.getFullName();

        callView(v -> v.openCommunityDialogs(accountId, groupId, subtitle));
    }

    public void fireGroupTokensReceived(ArrayList<Token> tokens) {
        for (Token token : tokens) {
            settings.registerAccountId(token.getOwnerId(), false);
            settings.storeAccessToken(token.getOwnerId(), token.getAccessToken());
        }

        if (tokens.size() == 1) {
            openCommunityMessages();
        }
    }

    public void fireAddToBookmarksClick() {
        final int accountId = super.getAccountId();

        appendDisposable(Apis.get()
                .vkDefault(accountId)
                .fave()
                .addGroup(Math.abs(ownerId))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignore -> safeShowToast(getView(), R.string.success, false),
                        throwable -> showError(getView(), getCauseIfRuntime(throwable))));
    }

    public void fireOptionMenuViewCreated(IGroupWallView.IOptionMenuView view) {
        view.setControlVisible(isAdmin());
    }

    public void fireChatClick() {
        final Peer peer = new Peer(ownerId).setTitle(community.getFullName()).setAvaUrl(community.getMaxSquareAvatar());
        final int accountId = super.getAccountId();
        getView().openChatWith(accountId, accountId, peer);
    }
}
package biz.dealnote.messenger.fragment.base;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import biz.dealnote.messenger.adapter.AttachmentsViewBinder;
import biz.dealnote.messenger.adapter.listener.OwnerClickListener;
import biz.dealnote.messenger.domain.ILikesInteractor;
import biz.dealnote.messenger.fragment.AbsWallFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 04.10.2016.
 * phoenix
 */
public abstract class PlaceSupportPresenterFragment<P extends PlaceSupportPresenter<V>, V extends IMvpView & IAttachmentsPlacesView & IAccountDependencyView>
        extends BasePresenterFragment<P, V> implements AttachmentsViewBinder.OnAttachmentsActionCallback, IAttachmentsPlacesView, OwnerClickListener {

    @Override
    public void savePresenterState(@NonNull P presenter, @NonNull Bundle outState) {
        presenter.saveState(outState);
    }

    @Override
    public void onOwnerClick(int ownerId) {
        getPresenter().fireOwnerClick(ownerId);
    }

    @Override
    public void openChatWith(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        PlaceFactory.getChatPlace(accountId, messagesOwnerId, peer).tryOpenWith(getActivity());
    }

    @Override
    public void onPollOpen(@NonNull Poll apiPoll) {
        getPresenter().firePollClick(apiPoll);
    }

    @Override
    public void onVideoPlay(@NonNull Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public void onAudioPlay(int position, @NonNull ArrayList<Audio> apiAudio) {
        getPresenter().fireAudioPlayClick(position, apiAudio);
    }

    @Override
    public void onForwardMessagesOpen(@NonNull ArrayList<Message> messages) {
        getPresenter().fireForwardMessagesClick(messages);
    }

    @Override
    public void onOpenOwner(int ownerId) {
        getPresenter().fireOwnerClick(ownerId);
    }

    @Override
    public void onDocPreviewOpen(@NonNull Document document) {
        getPresenter().fireDocClick(document);
    }

    @Override
    public void onPostOpen(@NonNull Post post) {
        getPresenter().firePostClick(post);
    }

    @Override
    public void onLinkOpen(@NonNull Link link) {
        getPresenter().fireLinkClick(link);
    }

    @Override
    public void onWikiPageOpen(@NonNull WikiPage page) {
        getPresenter().fireWikiPageClick(page);
    }

    @Override
    public void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index) {
        getPresenter().firePhotoClick(photos, index);
    }

    @Override
    public void openLink(int accountId, @NonNull Link link) {
        LinkHelper.openLinkInBrowser(getActivity(), link.getUrl());
    }

    @Override
    public void openWikiPage(int accountId, @NonNull WikiPage page) {
        PlaceFactory.getWikiPagePlace(accountId, page.getViewUrl())
                .tryOpenWith(getActivity());
    }

    @Override
    public void openSimplePhotoGallery(int accountId, @NonNull ArrayList<Photo> photos, int index, boolean needUpdate) {
        PlaceFactory.getSimpleGalleryPlace(accountId, photos, index, true).tryOpenWith(getActivity());
    }

    @Override
    public void openPost(int accountId, @NonNull Post post) {
        PlaceFactory.getPostPreviewPlace(accountId, post.getVkid(), post.getOwnerId(), post).tryOpenWith(getActivity());
    }

    @Override
    public void openDocPreview(int accountId, @NonNull Document document) {
        PlaceFactory.getDocPreviewPlace(accountId, document).tryOpenWith(getActivity());
    }

    @Override
    public void openOwnerWall(int accountId, int ownerId) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(getActivity());
    }

    @Override
    public void openForwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        PlaceFactory.getForwardMessagesPlace(accountId, messages).tryOpenWith(getActivity());
    }

    @Override
    public void playAudioList(int accountId, int position, @NonNull ArrayList<Audio> apiAudio) {
        MusicPlaybackService.startForPlayList(getActivity(), apiAudio, position, false);
        PlaceFactory.getPlayerPlace(accountId).tryOpenWith(getActivity());
    }

    @Override
    public void openVideo(int accountId, @NonNull Video apiVideo) {
        PlaceFactory.getVideoPreviewPlace(accountId, apiVideo).tryOpenWith(getActivity());
    }

    @Override
    public void openPoll(int accoountId, @NonNull Poll poll) {
        PlaceFactory.getPollPlace(accoountId, poll)
                .tryOpenWith(getActivity());

        //PlaceManager.withContext(getContext())
        //        .toPoll()
        ///       .withArguments(PollDialog.buildArgs(accoountId, poll, true))
        //        .open();
    }

    @Override
    public void openComments(int accountId, Commented commented, Integer focusToCommentId) {
        PlaceFactory.getCommentsPlace(accountId, commented, focusToCommentId)
                .tryOpenWith(getActivity());
    }

    @Override
    public void openSearch(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria) {
        PlaceFactory.getSingleTabSearchPlace(accountId, type, criteria).tryOpenWith(getActivity());
    }

    @Override
    public void goToLikes(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_LIKES)
                .tryOpenWith(getActivity());
    }

    @Override
    public void goToReposts(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_COPIES)
                .tryOpenWith(getActivity());
    }

    @Override
    public void repostPost(int accountId, @NonNull Post post) {
        AbsWallFragment.repost(getActivity(), accountId, post);
    }
}

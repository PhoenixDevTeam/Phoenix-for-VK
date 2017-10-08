package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.domain.IVideosInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IVideoPreviewView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public class VideoPreviewPresenter extends AccountDependencyPresenter<IVideoPreviewView> {

    private final int videoId;
    private final int ownerId;
    private final String accessKey;

    private Video video;

    private final IVideosInteractor interactor;

    public VideoPreviewPresenter(int accountId, int videoId, int ownerId, @Nullable Video video, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.interactor = InteractorFactory.createVideosInteractor();
        this.videoId = videoId;
        this.ownerId = ownerId;
        this.accessKey = nonNull(video) ? video.getAccessKey() : null;

        if (Objects.isNull(savedInstanceState)) {
            this.video = video;
        } else {
            this.video = savedInstanceState.getParcelable("video");
        }

        refreshVideoInfo();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable("video", video);
    }

    private boolean refreshingNow;

    private void setRefreshingNow(boolean refreshingNow) {
        this.refreshingNow = refreshingNow;
    }

    @OnGuiCreated
    private void resolveSubtitle() {
        if (isGuiReady()) getView().showSubtitle(nonNull(video) ? video.getTitle() : null);
    }

    @Override
    public void onGuiCreated(@NonNull IVideoPreviewView view) {
        super.onGuiCreated(view);

        if (nonNull(video)) {
            displayFullVideoInfo(view, video);
        } else if (refreshingNow) {
            view.displayLoading();
        } else {
            view.displayLoadingError();
        }
    }

    private void displayFullVideoInfo(IVideoPreviewView view, Video video) {
        view.displayVideoInfo(video);
        view.displayCommentCount(video.getCommentsCount());
        view.setCommentButtonVisible(video.isCanComment() || video.getCommentsCount() > 0 || isMy());
        view.displayLikes(video.getLikesCount(), video.isUserLikes());
    }

    private void onVideoInfoGetError(Throwable throwable) {
        setRefreshingNow(false);
        showError(getView(), throwable);

        if (Objects.isNull(video)) {
            callView(IVideoPreviewView::displayLoadingError);
        }
    }

    private void onActualInfoReceived(Video video) {
        setRefreshingNow(false);

        this.video = video;

        resolveSubtitle();
        callView(view -> displayFullVideoInfo(view, video));
    }

    private void refreshVideoInfo() {
        final int accountId = super.getAccountId();

        setRefreshingNow(true);

        if(isNull(video)){
            callView(IVideoPreviewView::displayLoading);
        }

        appendDisposable(interactor.getById(accountId, ownerId, videoId, accessKey, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualInfoReceived, throwable -> onVideoInfoGetError(getCauseIfRuntime(throwable))));
    }

    @Override
    protected String tag() {
        return VideoPreviewPresenter.class.getSimpleName();
    }

    private boolean isMy() {
        return super.getAccountId() == ownerId;
    }

    public void fireOptionViewCreated(IVideoPreviewView.IOptionView view) {
        view.setCanAdd(nonNull(video) && !isMy() && video.isCanAdd());
    }

    private void onAddComplete() {
        callView(IVideoPreviewView::showSuccessToast);
    }

    private void onAddError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireAddToMyClick() {
        final int accountId = super.getAccountId();

        appendDisposable(interactor.addToMy(accountId, accountId, ownerId, videoId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onAddComplete, throwable -> onAddError(getCauseIfRuntime(throwable))));
    }

    public void fireOwnerClick(int ownerId) {
        getView().showOwnerWall(getAccountId(), ownerId);
    }

    public void fireShareClick() {
        AssertUtils.requireNonNull(video);

        getView().displayShareDialog(getAccountId(), video, !isMy());
    }

    public void fireCommentsClick() {
        Commented commented = Commented.from(video);

        getView().showComments(getAccountId(), commented);
    }

    private void onLikesResponse(int count, boolean userLikes) {
        this.video.setLikesCount(count);
        this.video.setUserLikes(userLikes);

        callView(view -> view.displayLikes(count, userLikes));
    }

    private void onLikeError(Throwable throwable) {
        showError(getView(), throwable);
    }

    public void fireLikeClick() {
        AssertUtils.requireNonNull(video);

        final boolean add = !video.isUserLikes();
        final int accountId = super.getAccountId();

        appendDisposable(interactor.likeOrDislike(accountId, ownerId, videoId, accessKey, add)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onLikesResponse(pair.getFirst(), pair.getSecond()),
                        throwable -> onLikeError(getCauseIfRuntime(throwable))));
    }

    public void firePlayClick() {
        getView().showVideoPlayMenu(getAccountId(), video);
    }

    public void fireTryAgainClick() {
        refreshVideoInfo();
    }
}
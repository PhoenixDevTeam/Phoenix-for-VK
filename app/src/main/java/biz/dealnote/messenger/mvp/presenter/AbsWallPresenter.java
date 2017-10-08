package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.criteria.WallCriteria;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.IWallView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.CompareUtils;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.findIndexByPredicate;
import static biz.dealnote.messenger.util.Utils.findInfoByPredicate;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.intValueNotIn;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by ruslan.kolbasa on 23.01.2017.
 * phoenix
 */
public abstract class AbsWallPresenter<V extends IWallView> extends PlaceSupportPresenter<V> {

    private static final String TAG = AbsWallPresenter.class.getSimpleName();
    private static final int COUNT = 20;

    protected final int ownerId;

    protected final List<Post> wall;

    private final IWalls walls;

    private int wallFilter;
    protected boolean endOfContent;

    AbsWallPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.ownerId = ownerId;
        this.wall = new ArrayList<>();
        this.wallFilter = WallCriteria.MODE_ALL;
        this.walls = Injection.provideWalls();

        loadWallCachedData();
        requestWall(0);

        appendDisposable(walls
                .observeMinorChanges()
                .filter(update -> update.getAccountId() == getAccountId() && update.getOwnerId() == getOwnerId())
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostChange));

        appendDisposable(walls
                .observeChanges()
                .filter(post -> post.getOwnerId() == ownerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostChange));

        appendDisposable(walls
                .observePostInvalidation()
                .filter(pair -> pair.getOwnerId() == ownerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(pair -> onPostInvalid(pair.getId())));
    }

    private void onPostInvalid(int postVkid) {
        int index = findIndexByPredicate(wall, post -> post.getVkid() == postVkid);

        if (index != -1) {
            wall.remove(index);

            if (isGuiReady()) {
                getView().notifyWallItemRemoved(index);
            }
        }
    }

    private void onPostChange(Post post) {
        Pair<Integer, Post> found = findInfoByPredicate(wall, p -> p.getVkid() == post.getVkid());

        if (!isMatchFilter(post, wallFilter)) {
            // например, при публикации предложенной записи. Надо ли оно тут ?

            /*if (nonNull(found)) {
                int index = found.getFirst();
                wall.remove(index);

                if(isGuiReady()){
                    getView().notifyWallItemRemoved(index);
                }
            }*/

            return;
        }

        if (nonNull(found)) {
            int index = found.getFirst();
            wall.set(index, post);
            callView(view -> view.notifyWallItemChanged(index));
        } else {
            int targetIndex;

            if (!post.isPinned() && wall.size() > 0 && wall.get(0).isPinned()) {
                targetIndex = 1;
            } else {
                targetIndex = 0;
            }

            wall.add(targetIndex, post);
            callView(view -> view.notifyWallDataAdded(targetIndex, 1));
        }
    }

    private static boolean isMatchFilter(Post post, int filter) {
        switch (filter) {
            case WallCriteria.MODE_ALL:
                return intValueNotIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);

            case WallCriteria.MODE_OWNER:
                return post.getAuthorId() == post.getOwnerId()
                        && intValueNotIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);

            case WallCriteria.MODE_SCHEDULED:
                return post.getPostType() == VKApiPost.Type.POSTPONE;

            case WallCriteria.MODE_SUGGEST:
                return post.getPostType() == VKApiPost.Type.SUGGEST;
        }

        throw new IllegalArgumentException("Unknown filter");
    }

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayWallData(wall);
    }

    private CompositeDisposable cacheCompositeDisposable = new CompositeDisposable();
    private CompositeDisposable netCompositeDisposable = new CompositeDisposable();

    private void loadWallCachedData() {
        final int accountId = super.getAccountId();

        cacheCompositeDisposable.add(walls.getCachedWall(accountId, ownerId, wallFilter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Post> posts) {
        this.wall.clear();
        this.wall.addAll(posts);
        this.actualDataReady = false;

        callView(IWallView::notifyWallDataSetChanged);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    @Override
    public void onDestroyed() {
        cacheCompositeDisposable.dispose();
        super.onDestroyed();
    }

    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(requestNow && nowRequestOffset == 0);
        }
    }

    private void safeNotifyWallDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyWallDataSetChanged();
        }
    }

    private boolean requestNow;

    private int nowRequestOffset;

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;

        resolveRefreshingView();
        resolveLoadMoreFooterView();
    }

    private void setNowLoadingOffset(int offset) {
        this.nowRequestOffset = offset;
    }

    private void requestWall(int offset) {
        setNowLoadingOffset(offset);
        setRequestNow(true);

        final int accountId = super.getAccountId();
        final int nextOffset = offset + COUNT;
        final boolean append = offset > 0;

        netCompositeDisposable.add(walls.getWall(accountId, ownerId, offset, COUNT, wallFilter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(posts -> onActualDataReceived(nextOffset, posts, append), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable throwable) {
        setRequestNow(false);
        showError(getView(), getCauseIfRuntime(throwable));
    }

    private int nextOffset;

    private void onActualDataReceived(int nextOffset, List<Post> posts, boolean append) {
        this.cacheCompositeDisposable.clear();

        this.actualDataReady = true;
        this.nextOffset = nextOffset;
        this.endOfContent = posts.isEmpty();

        if (nonEmpty(posts)) {
            if (append) {
                int sizeBefore = this.wall.size();
                this.wall.addAll(posts);
                callView(view -> view.notifyWallDataAdded(sizeBefore, posts.size()));
            } else {
                this.wall.clear();
                this.wall.addAll(posts);
                callView(IWallView::notifyWallDataSetChanged);
            }
        }

        setRequestNow(false);
    }

    private boolean actualDataReady;

    @OnGuiCreated
    private void resolveLoadMoreFooterView() {
        if (isGuiReady()) {
            @LoadMoreState
            int state;

            if (requestNow) {
                if (nowRequestOffset == 0) {
                    state = LoadMoreState.INVISIBLE;
                } else {
                    state = LoadMoreState.LOADING;
                }
            } else if (endOfContent) {
                state = LoadMoreState.END_OF_LIST;
            } else {
                state = LoadMoreState.CAN_LOAD_MORE;
            }

            getView().setupLoadMoreFooter(state);
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private boolean canLoadMore() {
        return !endOfContent && actualDataReady && nonEmpty(wall) && !requestNow;
    }

    private void requestNext() {
        requestWall(nextOffset);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestNext();
        }
    }

    public void fireCreateClick() {
        getView().goToPostCreation(getAccountId(), ownerId, EditingPostType.DRAFT);
    }

    public final void fireRefresh() {
        this.netCompositeDisposable.clear();
        this.cacheCompositeDisposable.clear();

        requestWall(0);

        onRefresh();
    }

    protected void onRefresh(){

    }

    public void firePostBodyClick(Post post) {
        if (Utils.intValueIn(post.getPostType(), VKApiPost.Type.SUGGEST, VKApiPost.Type.POSTPONE)) {
            getView().openPostEditor(getAccountId(), post);
            return;
        }

        super.firePostClick(post);
    }

    public void firePostRestoreClick(Post post) {
        appendDisposable(walls.restore(getAccountId(), post.getOwnerId(), post.getVkid())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                }, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireLikeLongClick(Post post) {
        getView().goToLikes(getAccountId(), "post", post.getOwnerId(), post.getVkid());
    }

    public void fireShareLongClick(Post post) {
        getView().goToReposts(getAccountId(), "post", post.getOwnerId(), post.getVkid());
    }

    public void fireLikeClick(Post post) {
        final int accountId = super.getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(ignored -> {
                }, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    int getWallFilter() {
        return wallFilter;
    }

    boolean changeWallFilter(int mode) {
        boolean changed = mode != wallFilter;

        this.wallFilter = mode;

        if (changed) {
            cacheCompositeDisposable.clear();
            netCompositeDisposable.clear();

            loadWallCachedData();
            requestWall(0);
        }

        return changed;
    }

    boolean isMyWall() {
        return getAccountId() == ownerId;
    }

    private void onPostChange(PostUpdate update) {
        boolean pinStateChanged = nonNull(update.getPinUpdate());

        int index = findByVkid(update.getOwnerId(), update.getPostId());

        if (index != -1) {
            Post post = wall.get(index);

            if (nonNull(update.getLikeUpdate())) {
                post.setLikesCount(update.getLikeUpdate().getCount());
                post.setUserLikes(update.getLikeUpdate().isLiked());
            }

            if (nonNull(update.getDeleteUpdate())) {
                post.setDeleted(update.getDeleteUpdate().isDeleted());
            }

            if (nonNull(update.getPinUpdate())) {
                for (Post p : wall) {
                    p.setPinned(false);
                }

                post.setPinned(update.getPinUpdate().isPinned());
            }

            if (pinStateChanged) {
                Collections.sort(wall, COMPARATOR);
                safeNotifyWallDataSetChanged();
            } else {
                if (isGuiReady()) {
                    getView().notifyWallItemChanged(index);
                }
            }
        }
    }

    private static final Comparator<Post> COMPARATOR = (rhs, lhs) -> {
        if (rhs.isPinned() == lhs.isPinned()) {
            return CompareUtils.compareInts(lhs.getVkid(), rhs.getVkid());
        }

        return CompareUtils.compareBoolean(lhs.isPinned(), rhs.isPinned());
    };

    private int findByVkid(int ownerId, int vkid) {
        return Utils.indexOf(wall, post -> post.getOwnerId() == ownerId && post.getVkid() == vkid);
    }

    public void fireCopyUrlClick() {
        getView().copyToClipboard(getString(R.string.link), (isCommunity() ? "vk.com/club" : "vk.com/id") + Math.abs(ownerId));
    }

    public boolean isCommunity() {
        return ownerId < 0;
    }

    public void fireSearchClick() {
        getView().goToWallSearch(getAccountId(), getOwnerId());
    }

    public void fireButtonRemoveClick(Post post) {
        appendDisposable(walls.delete(getAccountId(), ownerId, post.getVkid())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                }, throwable -> showError(getView(), getCauseIfRuntime(throwable))));
    }
}
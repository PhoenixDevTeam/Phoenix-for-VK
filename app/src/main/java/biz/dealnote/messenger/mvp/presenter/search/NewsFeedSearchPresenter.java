package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.domain.IFeedInteractor;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.StringNextFrom;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.view.search.INewsFeedSearchView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Single;

/**
 * Created by admin on 03.10.2017.
 * phoenix
 */
public class NewsFeedSearchPresenter extends AbsSearchPresenter<INewsFeedSearchView, NewsFeedCriteria, Post, StringNextFrom> {

    private final IFeedInteractor feedInteractor;

    private final IWalls walls;

    public NewsFeedSearchPresenter(int accountId, @Nullable NewsFeedCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.feedInteractor = InteractorFactory.createFeedInteractor();
        this.walls = Injection.provideWalls();

        this.walls.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate);
    }

    private void onPostUpdate(PostUpdate update){
        // TODO: 03.10.2017
    }

    @Override
    StringNextFrom getInitialNextFrom() {
        return new StringNextFrom(null);
    }

    @Override
    boolean isAtLast(StringNextFrom startFrom) {
        return Utils.isEmpty(startFrom.getNextFrom());
    }

    @Override
    Single<Pair<List<Post>, StringNextFrom>> doSearch(int accountId, NewsFeedCriteria criteria, StringNextFrom startFrom) {
        return feedInteractor.search(accountId, criteria, 50, startFrom.getNextFrom())
                .map(pair -> Pair.create(pair.getFirst(), new StringNextFrom(pair.getSecond())));
    }

    @Override
    NewsFeedCriteria instantiateEmptyCriteria() {
        return new NewsFeedCriteria("");
    }

    @Override
    public void firePostClick(@NonNull Post post) {
        if (post.getPostType() == VKApiPost.Type.REPLY) {
            getView().openComments(getAccountId(), Commented.from(post), post.getVkid());
        } else {
            getView().openPost(getAccountId(), post);
        }
    }

    @Override
    boolean canSearch(NewsFeedCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }

    @Override
    protected String tag() {
        return NewsFeedSearchPresenter.class.getSimpleName();
    }

    public void fireLikeClick(Post post) {
        final int accountId = super.getAccountId();

        appendDisposable(walls.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(integer -> {/*ignore*/}, t -> showError(getView(), Utils.getCauseIfRuntime(t))));
    }
}
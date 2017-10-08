package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.WallAdapter;
import biz.dealnote.messenger.domain.ILikesInteractor;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.search.NewsFeedSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.INewsFeedSearchView;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class NewsFeedSearchFragment extends AbsSearchFragment<NewsFeedSearchPresenter, INewsFeedSearchView, Post>
        implements WallAdapter.ClickListener, INewsFeedSearchView {

    public static NewsFeedSearchFragment newInstance(int accountId, @Nullable NewsFeedCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        NewsFeedSearchFragment fragment = new NewsFeedSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Post> data) {
        ((WallAdapter)adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Post> data) {
        return new WallAdapter(getActivity(), data, this, this);
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        if (Utils.is600dp(getActivity())) {
            boolean land = Utils.isLandscape(getContext());
            return new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }
    }

    @Override
    public void onAvatarClick(int ownerId) {
        getPresenter().fireOwnerClick(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        getPresenter().fireShareClick(post);
    }

    @Override
    public void onPostClick(Post post) {
        getPresenter().firePostClick(post);
    }

    @Override
    public void onRestoreClick(Post post) {
        // not supported
    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_LIKES);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireCopiesLikesClick("post", post.getOwnerId(), post.getVkid(), ILikesInteractor.FILTER_COPIES);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }

    @Override
    public IPresenterFactory<NewsFeedSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new NewsFeedSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    protected String tag() {
        return NewsFeedSearchFragment.class.getSimpleName();
    }
}
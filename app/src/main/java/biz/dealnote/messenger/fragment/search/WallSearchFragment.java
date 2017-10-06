package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.WallAdapter;
import biz.dealnote.messenger.fragment.search.criteria.WallSearchCriteria;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.search.WallSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IWallSearchView;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public class WallSearchFragment extends AbsSearchFragment<WallSearchPresenter, IWallSearchView, Post>
        implements IWallSearchView, WallAdapter.ClickListener {

    public static WallSearchFragment newInstance(int accountId, WallSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        WallSearchFragment fragment = new WallSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public IPresenterFactory<WallSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            WallSearchCriteria c = getArguments().getParcelable(Extra.CRITERIA);
            return new WallSearchPresenter(accountId, c, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return WallSearchFragment.class.getSimpleName();
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Post> data) {
        ((WallAdapter) adapter).setItems(data);
    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Post> data) {
        WallAdapter adapter = new WallAdapter(getActivity(), data, this, this);
        return adapter;
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        RecyclerView.LayoutManager manager;

        if (Utils.is600dp(getActivity())) {
            boolean land = Utils.isLandscape(getActivity());
            manager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        }

        return manager;
    }

    @Override
    public void onOwnerClick(int ownerId) {
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

    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireShowLikesClick(post);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireShowCopiesClick(post);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }
}

package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.OwnersAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.SimpleOwnersPresenter;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public abstract class AbsOwnersListFragment<P extends SimpleOwnersPresenter<V>, V extends ISimpleOwnersView> extends BasePresenterFragment<P, V> implements ISimpleOwnersView {

    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected OwnersAdapter mOwnersAdapter;
    protected LinearLayoutManager mLinearLayoutManager;

    protected boolean mHasToolbar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(mHasToolbar ? R.layout.fragment_abs_friends_with_toolbar : R.layout.fragment_abs_friends, container, false);

        if(mHasToolbar){
            ((AppCompatActivity)getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        }

        mRecyclerView = root.findViewById(R.id.list);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        mLinearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
               getPresenter().fireScrollToEnd();
            }
        });

        mOwnersAdapter = new OwnersAdapter(getActivity(), Collections.emptyList());
        mOwnersAdapter.setClickListener(owner -> getPresenter().fireOwnerClick(owner));

        mRecyclerView.setAdapter(mOwnersAdapter);
        return root;
    }

    @Override
    public void displayOwnerList(List<Owner> owners) {
        if(Objects.nonNull(mOwnersAdapter)){
            mOwnersAdapter.setItems(owners);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mOwnersAdapter)){
            mOwnersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if(Objects.nonNull(mOwnersAdapter)){
            mOwnersAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void showOwnerWall(int accountId, Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, owner).tryOpenWith(getActivity());
    }
}
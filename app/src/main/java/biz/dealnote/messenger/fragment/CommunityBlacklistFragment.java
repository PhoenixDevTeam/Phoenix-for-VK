package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.SelectProfilesActivity;
import biz.dealnote.messenger.adapter.CommunityBannedAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.SelectProfileCriteria;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.CommunityBlacklistPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityBlacklistView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityBlacklistFragment extends BasePresenterFragment<CommunityBlacklistPresenter, ICommunityBlacklistView>
        implements ICommunityBlacklistView, CommunityBannedAdapter.ActionListener {

    private static final int REQUEST_SELECT_PROFILES = 17;

    public static CommunityBlacklistFragment newInstance(int accountId, int groupdId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupdId);
        CommunityBlacklistFragment fragment = new CommunityBlacklistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommunityBannedAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_blacklist, container, false);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToBottom();
            }
        });

        mAdapter = new CommunityBannedAdapter(getActivity(), Collections.emptyList());
        mAdapter.setActionListener(this);

        recyclerView.setAdapter(mAdapter);

        root.findViewById(R.id.button_add).setOnClickListener(v -> getPresenter().fireAddClick());
        return root;
    }

    @Override
    public IPresenterFactory<CommunityBlacklistPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityBlacklistPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }

    @Override
    protected String tag() {
        return CommunityBlacklistFragment.class.getSimpleName();
    }

    @Override
    public void displayRefreshing(boolean loadingNow) {
        if(Objects.nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(loadingNow);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void diplayData(List<Banned> data) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void openBanEditor(int accountId, int groupId, Banned banned) {
        PlaceFactory.getCommunityBanEditPlace(accountId, groupId, banned).tryOpenWith(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SELECT_PROFILES && resultCode == Activity.RESULT_OK){
            ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
            AssertUtils.requireNonNull(users);
            postPrenseterReceive(presenter -> presenter.fireAddToBanUsersSelected(users));
        }
    }

    @Override
    public void startSelectProfilesActivity(int accountId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        SelectProfileCriteria c = new SelectProfileCriteria();

        Place place = PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.PEOPLE, criteria);
        Intent intent = SelectProfilesActivity.createIntent(getActivity(), place, c);
        startActivityForResult(intent, REQUEST_SELECT_PROFILES);
    }

    @Override
    public void addUsersToBan(int accountId, int groupId, ArrayList<User> users) {
        PlaceFactory.getCommunityAddBanPlace(accountId, groupId, users).tryOpenWith(getActivity());
    }

    @Override
    public void notifyItemsAdded(int position, int size) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, size);
        }
    }

    @Override
    public void onBannedClick(Banned banned) {
        getPresenter().fireBannedClick(banned);
    }

    @Override
    public void onBannedLongClick(Banned banned) {
        String[] items = {getString(R.string.delete)};
        new AlertDialog.Builder(getActivity())
                .setTitle(banned.getUser().getFullName())
                .setItems(items, (dialog, which) -> getPresenter().fireBannedRemoveClick(banned))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
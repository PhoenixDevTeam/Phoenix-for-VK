package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.SelectProfilesActivity;
import biz.dealnote.messenger.adapter.PeopleAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.fragment.friends.FriendsTabsFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.SelectProfileCriteria;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.UserBannedPresenter;
import biz.dealnote.messenger.mvp.view.IUserBannedView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public class UserBannedFragment extends BaseMvpFragment<UserBannedPresenter, IUserBannedView> implements IUserBannedView, PeopleAdapter.LongClickListener {

    private static final int REQUEST_SELECT = 13;

    public static UserBannedFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        UserBannedFragment fragment = new UserBannedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;
    private PeopleAdapter mPeopleAdapter;
    private TextView mEmptyText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_banned, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        mRecyclerView = root.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mPeopleAdapter = new PeopleAdapter(requireActivity(), Collections.emptyList());
        mPeopleAdapter.setLongClickListener(this);
        mPeopleAdapter.setClickListener(owner -> getPresenter().fireUserClick((User) owner));
        mRecyclerView.setAdapter(mPeopleAdapter);

        mEmptyText = root.findViewById(R.id.empty_text);

        root.findViewById(R.id.button_add).setOnClickListener(v -> getPresenter().fireButtonAddClick());

        resolveEmptyTextVisibility();
        return root;
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mPeopleAdapter) && nonNull(mEmptyText)) {
            mEmptyText.setVisibility(mPeopleAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void displayUserList(List<User> users) {
        if (nonNull(mPeopleAdapter)) {
            mPeopleAdapter.setItems(users);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyItemsAdded(int position, int count) {
        if (nonNull(mPeopleAdapter)) {
            mPeopleAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mPeopleAdapter)) {
            mPeopleAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mPeopleAdapter)) {
            mPeopleAdapter.notifyItemRemoved(position);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.user_blacklist_title);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT && resultCode == Activity.RESULT_OK) {
            ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
            postPrenseterReceive(presenter -> presenter.fireUsersSelected(users));
        }
    }

    @Override
    public void startUserSelection(int accountId) {
        Place place = PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
        SelectProfileCriteria criteria = new SelectProfileCriteria();
        Intent intent = SelectProfilesActivity.createIntent(requireActivity(), place, criteria);
        startActivityForResult(intent, REQUEST_SELECT);
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(requireActivity(), R.string.success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void scrollToPosition(int position) {
        if(nonNull(mRecyclerView)){
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void showUserProfile(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public IPresenterFactory<UserBannedPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new UserBannedPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public boolean onOwnerLongClick(Owner owner) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(owner.getFullName())
                .setItems(new String[]{getString(R.string.delete)}, (dialog, which) -> getPresenter().fireRemoveClick((User) owner))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
        return true;
    }
}
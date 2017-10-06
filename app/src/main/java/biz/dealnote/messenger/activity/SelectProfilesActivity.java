package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.SelectedProfilesAdapter;
import biz.dealnote.messenger.fragment.friends.FriendsTabsFragment;
import biz.dealnote.messenger.model.SelectProfileCriteria;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

public class SelectProfilesActivity extends MainActivity implements SelectedProfilesAdapter.ActionListener, ProfileSelectable {

    private static final String TAG = SelectProfilesActivity.class.getSimpleName();

    private SelectProfileCriteria mSelectableCriteria;

    private ArrayList<User> mSelectedUsers;
    private RecyclerView mRecyclerView;
    private SelectedProfilesAdapter mProfilesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.mLayoutRes = R.layout.activity_main_with_profiles_selection;
        super.onCreate(savedInstanceState);
        super.mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;

        this.mSelectableCriteria = getIntent().getParcelableExtra(Extra.CRITERIA);

        if (savedInstanceState != null) {
            mSelectedUsers = savedInstanceState.getParcelableArrayList(SAVE_SELECTED_USERS);
        }

        if (mSelectedUsers == null) {
            mSelectedUsers = new ArrayList<>();
        }

        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mProfilesAdapter = new SelectedProfilesAdapter(this, mSelectedUsers);
        mProfilesAdapter.setActionListener(this);

        mRecyclerView = findViewById(R.id.recycleView);
        if (mRecyclerView == null) {
            throw new IllegalStateException("Invalid view");
        }

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mProfilesAdapter);
    }

    private static final String SAVE_SELECTED_USERS = "save_selected_users";

    /*public static void start(Activity activity, @Nullable ArrayList<VKApiUser> users, int requestCode){
        int aid = Accounts.getCurrentUid(activity);
        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        Intent intent = new Intent(activity, SelectProfilesActivity.class);
        intent.setAction(SelectProfilesActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putParcelableArrayListExtra(Extra.USERS, users);
        activity.startActivityForResult(intent, requestCode);
    }*/

    public static Intent createIntent(Context context, @NonNull Place initialPlace, @NonNull SelectProfileCriteria criteria) {
        return new Intent(context, SelectProfilesActivity.class)
                .setAction(SelectProfilesActivity.ACTION_OPEN_PLACE)
                .putExtra(Extra.PLACE, initialPlace)
                .putExtra(Extra.CRITERIA, criteria);
    }

    public static void startFriendsSelection(@NonNull Fragment fragment, int requestCode) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        Place place = PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null);

        SelectProfileCriteria criteria = new SelectProfileCriteria().setFriendsOnly(true);

        Intent intent = new Intent(fragment.getActivity(), SelectProfilesActivity.class);
        intent.setAction(SelectProfilesActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        intent.putExtra(Extra.CRITERIA, criteria);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_SELECTED_USERS, mSelectedUsers);
    }

    @Override
    public void onClick(int adapterPosition, User user) {
        mSelectedUsers.remove(mProfilesAdapter.toDataPosition(adapterPosition));
        mProfilesAdapter.notifyItemRemoved(adapterPosition);
        mProfilesAdapter.notifyHeaderChange();
    }

    @Override
    public void onCheckClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.USERS, mSelectedUsers);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void select(User user) {
        Logger.d(TAG, "Select, user: " + user);

        int index = Utils.indexOf(mSelectedUsers, user.getId());

        if (index != -1) {
            mSelectedUsers.remove(index);
            mProfilesAdapter.notifyItemRemoved(mProfilesAdapter.toAdapterPosition(index));
        }

        mSelectedUsers.add(0, user);
        mProfilesAdapter.notifyItemInserted(mProfilesAdapter.toAdapterPosition(0));
        mProfilesAdapter.notifyHeaderChange();
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public SelectProfileCriteria getAcceptableCriteria() {
        return mSelectableCriteria;
    }
}

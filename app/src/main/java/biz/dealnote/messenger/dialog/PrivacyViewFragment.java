package biz.dealnote.messenger.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.SelectProfilesActivity;
import biz.dealnote.messenger.adapter.PrivacyAdapter;
import biz.dealnote.messenger.api.model.VkApiPrivacy;
import biz.dealnote.messenger.dialog.base.AccountDependencyDialogFragment;
import biz.dealnote.messenger.model.FriendList;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.AssertUtils;

public class PrivacyViewFragment extends AccountDependencyDialogFragment implements PrivacyAdapter.ActionListener {

    private static final int REQUEST_CODE_ADD_TO_ALLOWED = 103;
    private static final int REQUEST_CODE_ADD_TO_DISALLOWED = 104;

    private static final String SAVE_PRIVACY = "save_privacy";
    private Privacy mPrivacy;
    private PrivacyAdapter mAdapter;

    public static Bundle buildArgs(int aid, Privacy privacy) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.PRIVACY, privacy);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static PrivacyViewFragment newInstance(Bundle args) {
        PrivacyViewFragment fragment = new PrivacyViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mPrivacy = savedInstanceState.getParcelable(SAVE_PRIVACY);
        }

        if (this.mPrivacy == null) {
            this.mPrivacy = clonePrivacyFromArgs();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = View.inflate(getActivity(), R.layout.fragment_privacy_view, null);

        int columns = getResources().getInteger(R.integer.privacy_entry_column_count);

        mAdapter = new PrivacyAdapter(getActivity(), mPrivacy);
        mAdapter.setActionListener(this);

        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.privacy_settings)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> returnResult())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void returnResult() {
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra(Extra.PRIVACY, mPrivacy);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    @Override
    public void onTypeClick() {
        String[] items = {
                getString(R.string.privacy_to_all_users),
                getString(R.string.privacy_to_friends_only),
                getString(R.string.privacy_to_friends_and_friends_of_friends),
                getString(R.string.privacy_to_only_me)
        };

        new AlertDialog.Builder(getActivity())
                .setItems(items, (dialog, which) -> {
                    switch (which){
                        case 0:
                            mPrivacy.setType(VkApiPrivacy.Type.ALL);
                            break;
                        case 1:
                            mPrivacy.setType(VkApiPrivacy.Type.FRIENDS);
                            break;
                        case 2:
                            mPrivacy.setType(VkApiPrivacy.Type.FRIENDS_OF_FRIENDS);
                            break;
                        case 3:
                            mPrivacy.setType(VkApiPrivacy.Type.ONLY_ME);
                            break;
                    }

                    safeNotifyDatasetChanged();
                }).setNegativeButton(R.string.button_cancel, null).show();
    }

    private void safeNotifyDatasetChanged() {
        if (isAdded() && mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAllowedUserRemove(User user) {
        mPrivacy.removeFromAllowed(user);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onAllowedFriendsListRemove(FriendList friendList) {
        mPrivacy.removeFromAllowed(friendList);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onDisallowedUserRemove(User user) {
        mPrivacy.removeFromDisallowed(user);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onDisallowedFriendsListRemove(FriendList friendList) {
        mPrivacy.removeFromDisallowed(friendList);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onAddToAllowedClick() {
        SelectProfilesActivity.startFriendsSelection(this, REQUEST_CODE_ADD_TO_ALLOWED);
    }

    @Override
    public void onAddToDisallowedClick() {
        SelectProfilesActivity.startFriendsSelection(this, REQUEST_CODE_ADD_TO_DISALLOWED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;

        ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
        AssertUtils.requireNonNull(users);

        switch (requestCode) {
            case REQUEST_CODE_ADD_TO_ALLOWED:
                for (User user : users) {
                    this.mPrivacy.allowFor(user);
                }

                break;
            case REQUEST_CODE_ADD_TO_DISALLOWED:
                for (User user : users) {
                    this.mPrivacy.disallowFor(user);
                }

                break;
        }

        safeNotifyDatasetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_PRIVACY, mPrivacy);
    }

    private Privacy clonePrivacyFromArgs() {
        Privacy privacy = getArguments().getParcelable(Extra.PRIVACY);
        if (privacy == null) {
            throw new IllegalArgumentException("Args do not contain Privacy extra");
        }

        try {
            return privacy.clone();
        } catch (CloneNotSupportedException e) {
            return privacy;
        }
    }
}

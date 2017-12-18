package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.SelectProfilesActivity;
import biz.dealnote.messenger.adapter.DialogsAdapter;
import biz.dealnote.messenger.dialog.DialogNotifOptionsDialog;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.DialogsSearchCriteria;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Dialog;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.DialogsPresenter;
import biz.dealnote.messenger.mvp.view.IDialogsView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.InputTextDialog;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by hp-dv6 on 05.06.2016.
 * VKMessenger
 */
public class DialogsFragment extends BasePresenterFragment<DialogsPresenter, IDialogsView>
        implements IDialogsView, DialogsAdapter.ClickListener {

    public static DialogsFragment newInstance(int accountId, int dialogsOwnerId, @Nullable String subtitle) {
        DialogsFragment fragment = new DialogsFragment();
        Bundle args = new Bundle();
        args.putString(Extra.SUBTITLE, subtitle);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, dialogsOwnerId);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private DialogsAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private FloatingActionButton mFab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialogs, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(DialogsAdapter.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        mFab = root.findViewById(R.id.fab);
        mFab.setOnClickListener(v -> createGroupChat());

        mAdapter = new DialogsAdapter(getActivity(), Collections.emptyList());
        mAdapter.setClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        return root;
    }

    private final RecyclerView.OnScrollListener mFabScrollListener = new RecyclerView.OnScrollListener() {
        int scrollMinOffset = 0;

        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
            if (scrollMinOffset == 0) {
                // one-time-init
                scrollMinOffset = (int) Utils.dpToPx(2, view.getContext());
            }

            if (dy > scrollMinOffset && mFab.isShown()) {
                mFab.hide();
            }

            if (dy < -scrollMinOffset && !mFab.isShown()) {
                mFab.show();
            }
        }
    };

    @Override
    public void setCreateGroupChatButtonVisible(boolean visible) {
        if (nonNull(mFab) && nonNull(mRecyclerView)) {
            mFab.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                mRecyclerView.addOnScrollListener(mFabScrollListener);
            } else {
                mRecyclerView.removeOnScrollListener(mFabScrollListener);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_dialogs, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        OptionView optionView = new OptionView();
        getPresenter().fireOptionViewCreated(optionView);
        menu.findItem(R.id.action_search).setVisible(optionView.canSearch);
    }

    private static final class OptionView implements IOptionView {

        boolean canSearch;

        @Override
        public void setCanSearch(boolean can) {
            this.canSearch = can;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                getPresenter().fireSearchClick();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_USERS_FOR_CHAT && resultCode == Activity.RESULT_OK) {
            ArrayList<User> users = data.getParcelableArrayListExtra(Extra.USERS);
            AssertUtils.requireNonNull(users);

            getPresenter().fireUsersForChatSelected(users);
        }
    }

    @Override
    public void onDialogClick(Dialog dialog) {
        getPresenter().fireDialogClick(dialog);
    }

    private static final class ContextView implements IContextView {

        boolean canDelete;
        boolean canAddToHomescreen;
        boolean canConfigNotifications;
        boolean canAddToShortcuts;

        @Override
        public void setCanDelete(boolean can) {
            this.canDelete = can;
        }

        @Override
        public void setCanAddToHomescreen(boolean can) {
            this.canAddToHomescreen = can;
        }

        @Override
        public void setCanConfigNotifications(boolean can) {
            this.canConfigNotifications = can;
        }

        @Override
        public void setCanAddToShortcuts(boolean can) {
            this.canAddToShortcuts = can;
        }
    }

    @Override
    public boolean onDialogLongClick(final Dialog dialog) {
        List<String> options = new ArrayList<>();

        ContextView contextView = new ContextView();
        getPresenter().fireContextViewCreated(contextView);

        final String delete = getString(R.string.delete);
        final String addToHomeScreen = getString(R.string.add_to_home_screen);
        final String notificationSettings = getString(R.string.peer_notification_settings);
        final String addToShortcuts = getString(R.string.add_to_launcer_shortcuts);

        if(contextView.canDelete){
            options.add(delete);
        }

        if(contextView.canAddToHomescreen){
            options.add(addToHomeScreen);
        }

        if(contextView.canConfigNotifications){
            options.add(notificationSettings);
        }

        if(contextView.canAddToShortcuts && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1){
            options.add(addToShortcuts);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(dialog.getDisplayTitle(getActivity()))
                .setItems(options.toArray(new String[options.size()]), (dialogInterface, which) -> {
                    final String selected = options.get(which);
                    if(selected.equals(delete)){
                        getPresenter().fireRemoveDialogClick(dialog);
                    } else if(selected.equals(addToHomeScreen)){
                        getPresenter().fireCreateShortcutClick(dialog);
                    } else if(selected.equals(notificationSettings)){
                        getPresenter().fireNotificationsSettingsClick(dialog);
                    } else if(selected.equals(addToShortcuts)){
                        getPresenter().fireAddToLauncherShortcuts(dialog);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();

        return options.size() > 0;
    }

    @Override
    public void onAvatarClick(Dialog dialog) {
        getPresenter().fireDialogAvatarClick(dialog);
    }

    private static final int REQUEST_CODE_SELECT_USERS_FOR_CHAT = 114;

    private void createGroupChat() {
        SelectProfilesActivity.startFriendsSelection(this, REQUEST_CODE_SELECT_USERS_FOR_CHAT);
    }

    @Override
    public void onDestroyView() {
        if (nonNull(mAdapter)) {
            mAdapter.cleanup();
        }

        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.destroyDrawingCache();
            mSwipeRefreshLayout.clearAnimation();
        }

        super.onDestroyView();
    }

    @Override
    protected String tag() {
        return DialogsFragment.class.getSimpleName();
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.DIALOGS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.dialogs);
            actionBar.setSubtitle(getArguments().getString(Extra.SUBTITLE));
        }

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onSectionResume(NavigationFragment.SECTION_ITEM_DIALOGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void displayData(List<Dialog> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void goToChat(int accountId, int messagesOwnerId, int peerId, String title, String avaurl) {
        PlaceFactory.getChatPlace(accountId, messagesOwnerId, new Peer(peerId).setTitle(title).setAvaUrl(avaurl)).tryOpenWith(getActivity());
    }

    @Override
    public void goToSearch(int accountId) {
        DialogsSearchCriteria criteria = new DialogsSearchCriteria("");

        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.DIALOGS, criteria)
                .tryOpenWith(getActivity());
    }

    @Override
    public void showSnackbar(@StringRes int res, boolean isLong) {
        View view = super.getView();
        if (nonNull(view)) {
            Snackbar.make(view, res, isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showEnterNewGroupChatTitle(List<User> users) {
        new InputTextDialog.Builder(getActivity())
                .setTitleRes(R.string.set_groupchat_title)
                .setAllowEmpty(true)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setCallback(newValue -> getPresenter().fireNewGroupChatTitleEntered(users, newValue))
                .show();
    }

    @Override
    public void showNotificationSettings(int accountId, int peerId) {
        DialogNotifOptionsDialog dialog = DialogNotifOptionsDialog.newInstance(accountId, peerId);
        dialog.show(getFragmentManager(), "dialog-notif-options");
    }

    @Override
    public void goToOwnerWall(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, owner).tryOpenWith(getActivity());
    }

    @Override
    public IPresenterFactory<DialogsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DialogsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }
}
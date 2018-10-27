package biz.dealnote.messenger.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.dialog.ResolveDomainDialog;
import biz.dealnote.messenger.fragment.AbsWallFragment;
import biz.dealnote.messenger.fragment.AudioPlayerFragment;
import biz.dealnote.messenger.fragment.AudiosFragment;
import biz.dealnote.messenger.fragment.BrowserFragment;
import biz.dealnote.messenger.fragment.ChatFragment;
import biz.dealnote.messenger.fragment.ChatUsersFragment;
import biz.dealnote.messenger.fragment.CommentsFragment;
import biz.dealnote.messenger.fragment.CommunitiesFragment;
import biz.dealnote.messenger.fragment.CommunityBanEditFragment;
import biz.dealnote.messenger.fragment.CommunityControlFragment;
import biz.dealnote.messenger.fragment.CommunityManagerEditFragment;
import biz.dealnote.messenger.fragment.CreatePhotoAlbumFragment;
import biz.dealnote.messenger.fragment.CreatePollFragment;
import biz.dealnote.messenger.fragment.DialogsFragment;
import biz.dealnote.messenger.fragment.DocPreviewFragment;
import biz.dealnote.messenger.fragment.DocsFragment;
import biz.dealnote.messenger.fragment.DrawerEditFragment;
import biz.dealnote.messenger.fragment.FeedFragment;
import biz.dealnote.messenger.fragment.FeedbackFragment;
import biz.dealnote.messenger.fragment.FwdsFragment;
import biz.dealnote.messenger.fragment.GifPagerFragment;
import biz.dealnote.messenger.fragment.LikesFragment;
import biz.dealnote.messenger.fragment.LogsFragement;
import biz.dealnote.messenger.fragment.MessagesLookFragment;
import biz.dealnote.messenger.fragment.NavigationFragment;
import biz.dealnote.messenger.fragment.NewsfeedCommentsFragment;
import biz.dealnote.messenger.fragment.NotificationPreferencesFragment;
import biz.dealnote.messenger.fragment.PhotoPagerFragment;
import biz.dealnote.messenger.fragment.PlaylistFragment;
import biz.dealnote.messenger.fragment.PollFragment;
import biz.dealnote.messenger.fragment.PreferencesFragment;
import biz.dealnote.messenger.fragment.RequestExecuteFragment;
import biz.dealnote.messenger.fragment.SecurityPreferencesFragment;
import biz.dealnote.messenger.fragment.TopicsFragment;
import biz.dealnote.messenger.fragment.UserBannedFragment;
import biz.dealnote.messenger.fragment.UserDetailsFragment;
import biz.dealnote.messenger.fragment.VKPhotoAlbumsFragment;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.fragment.VideoPreviewFragment;
import biz.dealnote.messenger.fragment.VideosFragment;
import biz.dealnote.messenger.fragment.VideosTabsFragment;
import biz.dealnote.messenger.fragment.WallPostFragment;
import biz.dealnote.messenger.fragment.attachments.CommentCreateFragment;
import biz.dealnote.messenger.fragment.attachments.CommentEditFragment;
import biz.dealnote.messenger.fragment.attachments.PostCreateFragment;
import biz.dealnote.messenger.fragment.attachments.PostEditFragment;
import biz.dealnote.messenger.fragment.attachments.RepostFragment;
import biz.dealnote.messenger.fragment.conversation.ConversationFragmentFactory;
import biz.dealnote.messenger.fragment.fave.FaveTabsFragment;
import biz.dealnote.messenger.fragment.friends.FriendsTabsFragment;
import biz.dealnote.messenger.fragment.search.SeachTabsFragment;
import biz.dealnote.messenger.fragment.search.SingleTabSeachFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.listener.AppStyleable;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserDetails;
import biz.dealnote.messenger.model.drawer.AbsDrawerItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.model.drawer.SectionDrawerItem;
import biz.dealnote.messenger.mvp.presenter.DocsListPresenter;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.place.PlaceProvider;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Accounts;
import biz.dealnote.messenger.util.Action;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.StatusbarUtil;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

public class MainActivity extends AppCompatActivity implements NavigationFragment.NavigationDrawerCallbacks,
        OnSectionResumeCallback, AppStyleable, PlaceProvider, ServiceConnection {

    public static final String ACTION_MAIN = "android.intent.action.MAIN";
    public static final String ACTION_CHAT_FROM_SHORTCUT = "biz.dealnote.messenger.ACTION_CHAT_FROM_SHORTCUT";
    public static final String ACTION_OPEN_PLACE = "biz.dealnote.messenger.activity.MainActivity.openPlace";
    public static final String ACTION_SEND_ATTACHMENTS = "biz.dealnote.messenger.ACTION_SEND_ATTACHMENTS";
    public static final String ACTION_SWITH_ACCOUNT = "biz.dealnote.messenger.ACTION_SWITH_ACCOUNT";

    public static final String EXTRA_NO_REQUIRE_PIN = "no_require_pin";

    /**
     * Extra with type {@link biz.dealnote.messenger.model.ModelsBundle} only
     */
    public static final String EXTRA_INPUT_ATTACHMENTS = "input_attachments";

    private static final String TAG = "MainActivity_LOG";

    private static final int REQUEST_LOGIN = 101;
    private static final int REQUEST_CODE_CLOSE = 102;
    private static final int REQUEST_ENTER_PIN = 103;

    protected static final int DOUBLE_BACK_PRESSED_TIMEOUT = 2000;

    protected int mAccountId;

    /**
     * Атрибуты секции, которая на данный момент находится на главном контейнере экрана
     */
    private AbsDrawerItem mCurrentFrontSection;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private MusicUtils.ServiceToken mAudioPlayServiceToken;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = () -> {
        resolveToolbarNavigationIcon();
        keyboardHide();
    };

    protected int mLayoutRes = R.layout.activity_main;
    private boolean mDestroyed;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().applyDayNight();

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onCurrentAccountChange));

        bindToAudioPlayService();

        setContentView(mLayoutRes);

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        setStatusbarColored(true, Settings.get().ui().isDarkModeEnabled(this));

        mDrawerLayout = findViewById(R.id.my_drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState != DrawerLayout.STATE_IDLE || mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    keyboardHide();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (drawerView.getId()) {
                    case R.id.navigation_drawer:
                        postResume(MainActivity::openTargetPage);
                        break;
                }
            }
        });

        getNavigationFragment().setUp(R.id.navigation_drawer, mDrawerLayout);
        getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
        resolveToolbarNavigationIcon();

        if (isNull(savedInstanceState)) {
            boolean intentWasHandled = handleIntent(getIntent());

            if (!intentWasHandled) {
                Place place = Settings.get().ui().getDefaultPage(mAccountId);
                place.tryOpenWith(this);
            }

            checkGCMRegistration();

            if (!isAuthValid()) {
                startAccountsActivity();
            } else {
                boolean needPin = Settings.get().security().isUsePinForEntrance()
                        && !getIntent().getBooleanExtra(EXTRA_NO_REQUIRE_PIN, false);
                if (needPin) {
                    startEnterPinActivity();
                }
            }
        }
    }

    private void postResume(Action<MainActivity> action) {
        if (resumed) {
            action.call(this);
        } else {
            postResumeActions.add(action);
        }
    }

    private List<Action<MainActivity>> postResumeActions = new ArrayList<>(0);

    private boolean resumed;

    @Override
    protected void onPause() {
        resumed = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        for (Action<MainActivity> action : postResumeActions) {
            action.call(this);
        }
        postResumeActions.clear();
    }

    private void startEnterPinActivity() {
        Intent intent = new Intent(this, EnterPinActivity.getClass(this));
        startActivityForResult(intent, REQUEST_ENTER_PIN);
    }

    private void checkGCMRegistration() {
        if (!checkPlayServices(this)) {
            Utils.showRedTopToast(this, R.string.this_device_does_not_support_gcm);
            return;
        }

        IPushRegistrationResolver resolver = Injection.providePushRegistrationResolver();

        mCompositeDisposable.add(resolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore()));

        //RequestHelper.checkPushRegistration(this);
    }

    private void bindToAudioPlayService() {
        if (!isActivityDestroyed()) {
            mAudioPlayServiceToken = MusicUtils.bindToServiceWithoutStart(this, this);
        }
    }

    private void resolveToolbarNavigationIcon() {
        if (isNull(mToolbar)) return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            Drawable backIcon = getFrontFragement() instanceof PhotoPagerFragment ||
                    getFrontFragement() instanceof GifPagerFragment ?
                    ContextCompat.getDrawable(this, R.drawable.arrow_left) :
                    CurrentTheme.getDrawableFromAttribute(this, R.attr.toolbarBackIcon);

            mToolbar.setNavigationIcon(backIcon);
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            mToolbar.setNavigationIcon(CurrentTheme.getDrawableFromAttribute(this, R.attr.toolbarDrawerIcon));
            mToolbar.setNavigationOnClickListener(v -> {
                if (mDrawerLayout.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_UNLOCKED) {
                    NavigationFragment navigationFragment = getNavigationFragment();

                    if (navigationFragment.isDrawerOpen()) {
                        navigationFragment.closeDrawer();
                    } else {
                        navigationFragment.openDrawer();
                    }
                }
            });
        }
    }

    private void onCurrentAccountChange(int newAccountId) {
        this.mAccountId = newAccountId;
        Accounts.showAccountSwitchedToast(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(TAG, "onNewIntent, intent: " + intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        if (ACTION_SWITH_ACCOUNT.equals(intent.getAction())) {
            int newAccountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            Settings.get()
                    .accounts()
                    .setCurrent(newAccountId);

            mAccountId = newAccountId;
            intent.setAction(ACTION_MAIN);
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();

        Logger.d(TAG, "handleIntent, extras: " + extras + ", action: " + action);

        if (extras != null) {
            if (ActivityUtils.checkInputExist(this)) {
                mCurrentFrontSection = NavigationFragment.SECTION_ITEM_DIALOGS;
                openDrawerPage(mCurrentFrontSection);
                return true;
            }
        }

        if (ACTION_SEND_ATTACHMENTS.equals(action)) {
            mCurrentFrontSection = NavigationFragment.SECTION_ITEM_DIALOGS;
            openDrawerPage(mCurrentFrontSection);
            return true;
        }

        if (ACTION_OPEN_PLACE.equals(action)) {
            Place place = intent.getParcelableExtra(Extra.PLACE);
            openPlace(place);
            return true;
        }

        if (ACTION_CHAT_FROM_SHORTCUT.equals(action)) {
            int aid = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int prefsAid = Settings.get()
                    .accounts()
                    .getCurrent();

            if (prefsAid != aid) {
                Settings.get()
                        .accounts()
                        .setCurrent(aid);
            }

            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            String title = intent.getStringExtra(Extra.TITLE);
            String imgUrl = intent.getStringExtra(Extra.IMAGE);

            final Peer peer = new Peer(peerId).setTitle(title).setAvaUrl(imgUrl);
            PlaceFactory.getChatPlace(aid, aid, peer).tryOpenWith(this);
            return true;
        }

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri data = intent.getData();
            LinkHelper.openUrl(this, mAccountId, String.valueOf(data));
            return true;
        }

        return false;
    }

    private void openTargetPage() {
        if (mTargetPage == null) {
            return;
        }

        AbsDrawerItem item = mTargetPage.getFirst();
        boolean clearBackStack = mTargetPage.getSecond();

        if (item.equals(mCurrentFrontSection)) {
            return;
        }

        if (item.getType() == AbsDrawerItem.TYPE_WITH_ICON || item.getType() == AbsDrawerItem.TYPE_WITHOUT_ICON) {
            openDrawerPage(item, clearBackStack);
        }

        if (item.getType() == AbsDrawerItem.TYPE_RECENT_CHAT) {
            openRecentChat((RecentChat) item);
        }

        mTargetPage = null;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (nonNull(mToolbar)) {
            mToolbar.setNavigationOnClickListener(null);
            mToolbar.setOnMenuItemClickListener(null);
        }

        super.setSupportActionBar(toolbar);

        mToolbar = toolbar;
        resolveToolbarNavigationIcon();
    }

    private void openChat(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        RecentChat recentChat = new RecentChat(accountId, peer.getId(), peer.getTitle(), peer.getAvaUrl());

        getNavigationFragment().appendRecentChat(recentChat);
        getNavigationFragment().refreshDrawerItems();
        getNavigationFragment().selectPage(recentChat);

        Fragment fragment = getFrontFragement();

        if (fragment instanceof ChatFragment) {
            Logger.d(TAG, "Chat fragment is present. Try to re-init");
            ChatFragment chatFragment = (ChatFragment) fragment;
            chatFragment.reInit(accountId, messagesOwnerId, peer);
            onChatResume(accountId, peer.getId(), peer.getTitle(), peer.getAvaUrl());
        } else {
            Logger.d(TAG, "Create new chat fragment");

            ChatFragment chatFragment = ChatFragment.Companion.newInstance(accountId, messagesOwnerId, peer);
            attachToFront(chatFragment);
        }
    }

    private void openRecentChat(RecentChat chat) {
        final int accountId = this.mAccountId;
        final int messagesOwnerId = this.mAccountId;
        openChat(accountId, messagesOwnerId, new Peer(chat.getPeerId()).setAvaUrl(chat.getIconUrl()).setTitle(chat.getTitle()));
    }

    private NavigationFragment getNavigationFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (NavigationFragment) fm.findFragmentById(R.id.navigation_drawer);
    }

    private void openDrawerPage(@NonNull AbsDrawerItem item) {
        openDrawerPage(item, true);
    }

    private void openDrawerPage(@NonNull AbsDrawerItem item, boolean clearBackStack) {
        if (item.getType() == AbsDrawerItem.TYPE_RECENT_CHAT) {
            openRecentChat((RecentChat) item);
            return;
        }

        SectionDrawerItem sectionDrawerItem = (SectionDrawerItem) item;
        if (sectionDrawerItem.getSection() == NavigationFragment.PAGE_ACCOUNTS) {
            startAccountsActivity();
            return;
        }

        if (sectionDrawerItem.getSection() == NavigationFragment.PAGE_BUY_FULL_APP) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PreferencesFragment.FULL_APP_URL));
            startActivity(browserIntent);
            return;
        }

        mCurrentFrontSection = item;
        getNavigationFragment().selectPage(item); // TODO NavigationFragment can bee NULL. WTF?

        if (clearBackStack) {
            clearBackStack();
        }

        final int aid = mAccountId;

        switch (sectionDrawerItem.getSection()) {
            case NavigationFragment.PAGE_DIALOGS:
                openPlace(PlaceFactory.getDialogsPlace(aid, aid, null));
                break;
            case NavigationFragment.PAGE_FRIENDS:
                openPlace(PlaceFactory.getFriendsFollowersPlace(aid, aid, FriendsTabsFragment.TAB_ALL_FRIENDS, null));
                break;
            case NavigationFragment.PAGE_GROUPS:
                openPlace(PlaceFactory.getCommunitiesPlace(aid, aid));
                break;
            case NavigationFragment.PAGE_PREFERENSES:
                openPlace(PlaceFactory.getPreferencesPlace(aid));
                break;
            case NavigationFragment.PAGE_MUSIC:
                openPlace(PlaceFactory.getAudiosPlace(aid, aid));
                break;
            case NavigationFragment.PAGE_DOCUMENTS:
                openPlace(PlaceFactory.getDocumentsPlace(aid, aid, DocsListPresenter.ACTION_SHOW));
                break;
            case NavigationFragment.PAGE_FEED:
                openPlace(PlaceFactory.getFeedPlace(aid));
                break;
            case NavigationFragment.PAGE_NOTIFICATION:
                openPlace(PlaceFactory.getNotificationsPlace(aid));
                break;
            case NavigationFragment.PAGE_PHOTOS:
                openPlace(PlaceFactory.getVKPhotoAlbumsPlace(aid, aid, VKPhotosFragment.ACTION_SHOW_PHOTOS, null));
                break;
            case NavigationFragment.PAGE_VIDEOS:
                openPlace(PlaceFactory.getVideosPlace(aid, aid, VideosFragment.ACTION_SHOW));
                break;
            case NavigationFragment.PAGE_BOOKMARKS:
                openPlace(PlaceFactory.getBookmarksPlace(aid, FaveTabsFragment.TAB_PHOTOS));
                break;
            case NavigationFragment.PAGE_SEARCH:
                openPlace(PlaceFactory.getSearchPlace(aid, SeachTabsFragment.TAB_PEOPLE, null));
                break;
            case NavigationFragment.PAGE_NEWSFEED_COMMENTS:
                openPlace(PlaceFactory.getNewsfeedCommentsPlace(aid));
                break;
            default:
                throw new IllegalArgumentException("Unknown place!!! " + item);
        }
    }

    private void startAccountsActivity() {
        Intent intent = new Intent(this, AccountsActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        /*if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }*/

        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // TODO: 13.12.2017 Exception java.lang.IllegalStateException:Can not perform this action after onSaveInstanceState
        Logger.d(TAG, "Back stack was cleared");
    }

    /**
     * First - DrawerItem, second - Clear back stack before adding
     */
    private Pair<AbsDrawerItem, Boolean> mTargetPage;

    @Override
    public void onNavigationDrawerItemSelected(AbsDrawerItem item, boolean longClick) {
        if (mCurrentFrontSection != null && mCurrentFrontSection.equals(item)) {
            return;
        }

        mTargetPage = Pair.Companion.create(item, !longClick);

        if (mDrawerLayout == null) {
            openTargetPage();
        }

        //после закрытия бокового меню откроется данная страница
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        mDestroyed = true;

        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);

        unbindFromAudioPlayService();
        super.onDestroy();
    }

    private void unbindFromAudioPlayService() {
        if (mAudioPlayServiceToken != null) {
            MusicUtils.unbindFromService(mAudioPlayServiceToken);
            mAudioPlayServiceToken = null;
        }
    }

    private boolean isAuthValid() {
        return mAccountId != ISettings.IAccountsSettings.INVALID_ID;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPerms.tryInterceptAppPermission(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOGIN:
                mAccountId = Settings.get()
                        .accounts()
                        .getCurrent();

                if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
                    supportFinishAfterTransition();
                }
                break;

            case REQUEST_CODE_CLOSE:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
            case REQUEST_ENTER_PIN:
                if (resultCode != RESULT_OK) {
                    finish();
                }
                break;
        }
    }

    public void keyboardHide() {
        try {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception ignored) {

        }
    }

    private Fragment getFrontFragement() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onBackPressed() {
        if (getNavigationFragment().isDrawerOpen()) {
            getNavigationFragment().closeDrawer();
            return;
        }

        Fragment front = getFrontFragement();
        if (front instanceof BackPressCallback) {
            if (!(((BackPressCallback) front).onBackPressed())) {
                return;
            }
        }

        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (mLastBackPressedTime < 0
                    || mLastBackPressedTime + DOUBLE_BACK_PRESSED_TIMEOUT > System.currentTimeMillis()
                    || !Settings.get().main().isNeedDoublePressToExit()) {
                supportFinishAfterTransition();
                return;
            }

            this.mLastBackPressedTime = System.currentTimeMillis();
            Toast.makeText(this, getString(R.string.click_back_to_exit), Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    protected long mLastBackPressedTime;

    @Override
    public boolean onNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    /* Убрать выделение в боковом меню */
    private void resetDrawerSelection() {
        mCurrentFrontSection = null;
        getNavigationFragment().selectPage(null);
    }

    @Override
    public void onSectionResume(SectionDrawerItem sectionDrawerItem) {
        getNavigationFragment().selectPage(sectionDrawerItem);
        mCurrentFrontSection = sectionDrawerItem;
    }

    @Override
    public void onChatResume(int accountId, int peerId, String title, String imgUrl) {
        RecentChat recentChat = new RecentChat(accountId, peerId, title, imgUrl);
        getNavigationFragment().appendRecentChat(recentChat);
        getNavigationFragment().refreshDrawerItems();
        getNavigationFragment().selectPage(recentChat);
        mCurrentFrontSection = recentChat;
    }

    @Override
    public void onClearSelection() {
        resetDrawerSelection();
        mCurrentFrontSection = null;
    }

    private void attachToFront(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void setStatusbarColored(boolean colored, boolean invertIcons) {
        int statusbarNonColored = CurrentTheme.getStatusBarNonColored(this);

        if (Utils.hasLollipop()) {
            Window w = getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(colored ? Color.TRANSPARENT : statusbarNonColored);
            int navigationColor = colored ? CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
            w.setNavigationBarColor(navigationColor);
        }

        if (Utils.hasMarshmallow()) {
            if (invertIcons) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            } else {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }

            StatusbarUtil.setCustomStatusbarDarkMode(this, invertIcons);
        }

        if (Utils.hasOreo()) {
            Window w = getWindow();
            if (invertIcons) {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                w.setNavigationBarColor(Color.WHITE);
            } else {
                int flags = getWindow().getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                @ColorInt
                int navigationColor = colored ?
                        CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
                w.setNavigationBarColor(navigationColor);
            }
        }
    }

    @Override
    public void blockDrawer(boolean block, int gravity) {
        if (block) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, gravity);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, gravity);
        }
    }

    @Override
    public void openDrawer(boolean open, int gravity) {
        if (open) {
            mDrawerLayout.openDrawer(gravity);
        } else {
            mDrawerLayout.closeDrawer(gravity);
        }
    }

    @Override
    public void openPlace(Place place) {
        final Bundle args = place.getArgs();
        switch (place.type) {
            case Place.VIDEO_PREVIEW:
                attachToFront(VideoPreviewFragment.newInstance(args));
                break;

            case Place.FRIENDS_AND_FOLLOWERS:
                attachToFront(FriendsTabsFragment.newInstance(args));
                break;

            case Place.WIKI_PAGE:
                attachToFront(BrowserFragment.newInstance(args));
                break;

            case Place.EXTERNAL_LINK:
                attachToFront(BrowserFragment.newInstance(args));
                break;

            case Place.DOC_PREVIEW:
                Document document = args.getParcelable(Extra.DOC);
                if (document != null && document.hasValidGifVideoLink()) {
                    int aid = args.getInt(Extra.ACCOUNT_ID);
                    ArrayList<Document> documents = new ArrayList<>(Collections.singletonList(document));

                    Bundle argsForGifs = GifPagerFragment.buildArgs(aid, documents, 0);
                    attachToFront(GifPagerFragment.newInstance(argsForGifs));
                } else {
                    attachToFront(DocPreviewFragment.newInstance(args));
                }
                break;

            case Place.WALL_POST:
                attachToFront(WallPostFragment.newInstance(args));
                break;

            case Place.COMMENTS:
                attachToFront(CommentsFragment.newInstance(place));
                break;

            case Place.WALL:
                attachToFront(AbsWallFragment.newInstance(args));
                break;

            case Place.CONVERSATION_ATTACHMENTS:
                attachToFront(ConversationFragmentFactory.newInstance(args));
                break;

            case Place.PLAYER:
                if (!(getFrontFragement() instanceof AudioPlayerFragment)) {
                    attachToFront(AudioPlayerFragment.newInstance(args));
                }
                break;

            case Place.CHAT:
                final Peer peer = args.getParcelable(Extra.PEER);
                AssertUtils.requireNonNull(peer);
                openChat(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID), peer);
                break;

            case Place.SEARCH:
                attachToFront(SeachTabsFragment.newInstance(args));
                break;

            case Place.BUILD_NEW_POST:
                PostCreateFragment postCreateFragment = PostCreateFragment.newInstance(args);
                place.applyTargetingTo(postCreateFragment);
                attachToFront(postCreateFragment);
                break;

            case Place.EDIT_COMMENT: {
                Comment comment = args.getParcelable(Extra.COMMENT);
                int accountId = args.getInt(Extra.ACCOUNT_ID);
                CommentEditFragment commentEditFragment = CommentEditFragment.newInstance(accountId, comment);
                place.applyTargetingTo(commentEditFragment);
                attachToFront(commentEditFragment);
            }
            break;

            case Place.EDIT_POST:
                PostEditFragment postEditFragment = PostEditFragment.newInstance(args);
                place.applyTargetingTo(postEditFragment);
                attachToFront(postEditFragment);
                break;

            case Place.REPOST:
                attachToFront(RepostFragment.obtain(place));
                break;

            case Place.DIALOGS:
                attachToFront(DialogsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getString(Extra.SUBTITLE)
                ));
                break;

            case Place.FORWARD_MESSAGES:
                attachToFront(FwdsFragment.newInstance(args));
                break;

            case Place.TOPICS:
                attachToFront(TopicsFragment.newInstance(args));
                break;

            case Place.CHAT_MEMBERS:
                attachToFront(ChatUsersFragment.newInstance(args));
                break;

            case Place.COMMUNITIES:
                CommunitiesFragment communitiesFragment = CommunitiesFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.USER_ID)
                );

                attachToFront(communitiesFragment);
                break;

            case Place.AUDIOS:
                attachToFront(AudiosFragment.newInstance(args.getInt(Extra.ACCOUNT_ID), args.getInt(Extra.OWNER_ID)));
                break;

            case Place.VIDEO_ALBUM:
                attachToFront(VideosFragment.newInstance(args));
                break;

            case Place.VIDEOS:
                attachToFront(VideosTabsFragment.newInstance(args));
                break;

            case Place.VK_PHOTO_ALBUMS:
                attachToFront(VKPhotoAlbumsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getString(Extra.ACTION),
                        args.getParcelable(Extra.OWNER)
                ));
                break;

            case Place.VK_PHOTO_ALBUM:
                attachToFront(VKPhotosFragment.newInstance(args));
                break;

            case Place.VK_PHOTO_ALBUM_GALLERY:
                attachToFront(PhotoPagerFragment.newInstance(place.type, args));
                break;

            case Place.FAVE_PHOTOS_GALLERY:
                attachToFront(PhotoPagerFragment.newInstance(place.type, args));
                break;

            case Place.SIMPLE_PHOTO_GALLERY:
                attachToFront(PhotoPagerFragment.newInstance(place.type, args));
                break;

            case Place.VK_PHOTO_TMP_SOURCE:
                attachToFront(PhotoPagerFragment.newInstance(place.type, args));
                break;

            case Place.POLL:
                attachToFront(PollFragment.newInstance(args));
                break;

            case Place.BOOKMARKS:
                attachToFront(FaveTabsFragment.newInstance(args));
                break;

            case Place.DOCS:
                attachToFront(DocsFragment.newInstance(args));
                break;

            case Place.FEED:
                attachToFront(FeedFragment.newInstance(args));
                break;

            case Place.NOTIFICATIONS:
                attachToFront(FeedbackFragment.newInstance(args));
                break;

            case Place.PREFERENCES:
                attachToFront(PreferencesFragment.newInstance(args));
                break;

            case Place.RESOLVE_DOMAIN:
                ResolveDomainDialog domainDialog = ResolveDomainDialog.newInstance(args);
                domainDialog.show(getSupportFragmentManager(), "resolve-domain");
                break;

            case Place.VK_INTERNAL_PLAYER:
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtras(args);
                startActivity(intent);
                break;

            case Place.NOTIFICATION_SETTINGS:
                attachToFront(new NotificationPreferencesFragment());
                break;

            case Place.LIKES_AND_COPIES:
                attachToFront(LikesFragment.newInstance(args));
                break;

            case Place.CREATE_PHOTO_ALBUM:
            case Place.EDIT_PHOTO_ALBUM:
                CreatePhotoAlbumFragment createPhotoAlbumFragment = CreatePhotoAlbumFragment.newInstance(args);
                place.applyTargetingTo(createPhotoAlbumFragment);
                attachToFront(createPhotoAlbumFragment);
                break;

            case Place.MESSAGE_LOOKUP:
                attachToFront(MessagesLookFragment.newInstance(args));
                break;

            case Place.AUDIO_CURRENT_PLAYLIST:
                attachToFront(PlaylistFragment.newInstance(args));
                break;

            case Place.GIF_PAGER:
                attachToFront(GifPagerFragment.newInstance(args));
                break;

            case Place.SECURITY:
                attachToFront(new SecurityPreferencesFragment());
                break;

            case Place.CREATE_POLL:
                CreatePollFragment createPollFragment = CreatePollFragment.newInstance(args);
                place.applyTargetingTo(createPollFragment);
                attachToFront(createPollFragment);
                break;

            case Place.COMMENT_CREATE:
                openCommentCreatePlace(place);
                break;

            case Place.LOGS:
                attachToFront(LogsFragement.newInstance());
                break;

            case Place.SINGLE_SEARCH:
                SingleTabSeachFragment singleTabSeachFragment = SingleTabSeachFragment.newInstance(args);
                attachToFront(singleTabSeachFragment);
                break;

            case Place.NEWSFEED_COMMENTS:
                NewsfeedCommentsFragment newsfeedCommentsFragment = NewsfeedCommentsFragment.newInstance(args.getInt(Extra.ACCOUNT_ID));
                attachToFront(newsfeedCommentsFragment);
                break;

            case Place.COMMUNITY_CONTROL:
                CommunityControlFragment communityControlFragment = CommunityControlFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getParcelable(Extra.OWNER),
                        args.getParcelable(Extra.SETTINGS)
                );
                attachToFront(communityControlFragment);
                break;

            case Place.COMMUNITY_BAN_EDIT:
                CommunityBanEditFragment communityBanEditFragment = CommunityBanEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        (Banned) args.getParcelable(Extra.BANNED)
                );
                attachToFront(communityBanEditFragment);
                break;

            case Place.COMMUNITY_ADD_BAN:
                attachToFront(CommunityBanEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        args.getParcelableArrayList(Extra.USERS)
                ));
                break;

            case Place.COMMUNITY_MANAGER_ADD:
                attachToFront(CommunityManagerEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        args.getParcelableArrayList(Extra.USERS)
                ));
                break;

            case Place.COMMUNITY_MANAGER_EDIT:
                attachToFront(CommunityManagerEditFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.GROUP_ID),
                        (Manager) args.getParcelable(Extra.MANAGER)
                ));
                break;

            case Place.REQUEST_EXECUTOR:
                attachToFront(RequestExecuteFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.USER_BLACKLIST:
                attachToFront(UserBannedFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)));
                break;

            case Place.DRAWER_EDIT: {
                attachToFront(DrawerEditFragment.newInstance());
            }
            break;

            case Place.USER_DETAILS: {
                int accountId = args.getInt(Extra.ACCOUNT_ID);
                User user = args.getParcelable(Extra.USER);
                UserDetails details = args.getParcelable("details");
                attachToFront(UserDetailsFragment.newInstance(accountId, user, details));
            }
            break;

            default:
                throw new IllegalArgumentException("Main activity can't open this place, type: " + place.type);
        }
    }

    private void openCommentCreatePlace(Place place) {
        CommentCreateFragment fragment = CommentCreateFragment.newInstance(
                place.getArgs().getInt(Extra.ACCOUNT_ID),
                place.getArgs().getInt(Extra.COMMENT_ID),
                place.getArgs().getInt(Extra.OWNER_ID),
                place.getArgs().getString(Extra.BODY)
        );

        place.applyTargetingTo(fragment);
        attachToFront(fragment);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (name.getClassName().equals(MusicPlaybackService.class.getName())) {
            Logger.d(TAG, "Connected to MusicPlaybackService");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (isActivityDestroyed()) return;

        if (name.getClassName().equals(MusicPlaybackService.class.getName())) {
            Logger.d(TAG, "Disconnected from MusicPlaybackService");
            mAudioPlayServiceToken = null;
            bindToAudioPlayService();
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private boolean isActivityDestroyed() {
        return mDestroyed;
    }

    private void showCommunityInviteDialog() {
        // чтобы при повторном вызове onWindowFocusChanged не отобразился этот диалог
        Settings.get().main().incrementRunCount();

        new AlertDialog.Builder(this)
                .setTitle(R.string.app_community_invite_title)
                .setMessage(R.string.app_community_invite_message)
                .setPositiveButton(R.string.button_go, (dialog, which) -> {
                    Settings.get().main().setRunCount(1000);
                    goToAppCommunity();
                })
                .setNegativeButton(R.string.never, (dialog, which) -> Settings.get().main().setRunCount(1000))
                .setNeutralButton(R.string.later, null)
                .show();
    }

    private void goToAppCommunity() {
        PlaceFactory.getOwnerWallPlace(mAccountId, -PreferencesFragment.APP_GROUP_ID, null)
                .tryOpenWith(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            int runCount = Settings.get().main().getRunCount();
            if (runCount % 10 == 0 && runCount < 35) {
                showCommunityInviteDialog();
            }
        }
    }
}
package biz.dealnote.messenger.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Collections;

import biz.dealnote.messenger.BuildConfig;
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
import biz.dealnote.messenger.service.CheckLicenseService;
import biz.dealnote.messenger.settings.AppPrefs;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.UploadService;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.Accounts;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
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
    private BroadcastReceiver mMultipleActionsReceiver;
    private DrawerLayout mDrawerLayout;
    private MusicUtils.ServiceToken mAudioPlayServiceToken;
    private UploadUtils.ServiceToken mUploadServiceToken;

    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = () -> {
        resolveToolbarNavigationIcon();
        keyboardHide();
    };

    protected int mLayoutRes = R.layout.activity_main;
    private ActionMode mActionMode;
    private boolean mDestroyed;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMultipleActionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                switch (intent.getAction()) {
                    case CheckLicenseService.BROADCAST_ACTION:
                        boolean success = intent.getBooleanExtra(CheckLicenseService.EXTRA_LICENSE_SUCCESS, true);
                        if (!success && !isFinishing() && AppPrefs.FULL_APP) {
                            showBuyDialog();
                        }
                        break;
                }
            }
        };

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onCurrentAccountChange));

        IntentFilter filter = new IntentFilter();
        filter.addAction(CheckLicenseService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMultipleActionsReceiver, filter);

        bindToAudioPlayService();
        bindToUploadService();

        setTheme(Settings.get()
                .ui()
                .getMainTheme());

        setContentView(mLayoutRes);

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        setStatusbarColored(true, Settings.get().ui().isMonochromeWhite());

        mDrawerLayout = findViewById(R.id.my_drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState != DrawerLayout.STATE_IDLE || mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    if (Objects.nonNull(mActionMode)) {
                        mActionMode.finish();
                    }

                    keyboardHide();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                switch (drawerView.getId()) {
                    case R.id.navigation_drawer:
                        openTargetPage();
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

                //openDrawerPage(mCurrentFrontSection);
            }

            if (AppPrefs.FULL_APP && !BuildConfig.DEBUG) {
                checkLicense();
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
                .subscribe(() -> {/*ignore*/}, Throwable::printStackTrace));

        //RequestHelper.checkPushRegistration(this);
    }

    private void bindToUploadService() {
        if (!isActivityDestroyed()) {
            mUploadServiceToken = UploadUtils.bindToService(this, this);
        }
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

    private void checkLicense() {
        startService(new Intent(this, CheckLicenseService.class));
    }

    private void showBuyDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.application_not_licensed_title)
                .setCancelable(false)
                .setMessage(R.string.application_not_licensed_message)
                .setPositiveButton(R.string.buy_app,
                        (dialog, which) -> {
                            goToGooglePlayFull();
                            finish();
                        })
                .setNegativeButton(R.string.exit, (dialog, which) -> finish()).show();
    }

    private void goToGooglePlayFull() {
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/details?id=" + getPackageName()));
        startActivity(marketIntent);
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

    @Override
    public void onSupportActionModeStarted(@NonNull ActionMode mode) {
        super.onSupportActionModeStarted(mode);
        mActionMode = mode;

        if (Utils.hasLollipop()) {
            getWindow().setStatusBarColor(CurrentTheme.getColorPrimaryDark(this));
        }
    }

    @Override
    public void onSupportActionModeFinished(@NonNull ActionMode mode) {
        super.onSupportActionModeFinished(mode);
        mActionMode = null;
        if (Utils.hasLollipop()) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
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
            chatFragment.reInit(accountId, messagesOwnerId, peer.getId(), peer.getTitle());
            onChatResume(accountId, peer.getId(), peer.getTitle(), peer.getAvaUrl());
        } else {
            Logger.d(TAG, "Create new chat fragment");

            ChatFragment chatFragment = ChatFragment.newInstance(accountId, messagesOwnerId, peer);
            attachFragment(chatFragment, true, "chat");
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

        mTargetPage = Pair.create(item, !longClick);

        if (mDrawerLayout == null) {
            openTargetPage();
        }

        //после закрытия бокового меню откроется данная страница
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        mDestroyed = true;

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMultipleActionsReceiver);
        getSupportFragmentManager().removeOnBackStackChangedListener(mOnBackStackChangedListener);

        unbindFromUploadService();
        unbindFromAudioPlayService();
        super.onDestroy();
    }

    private void unbindFromUploadService() {
        if (mUploadServiceToken != null) {
            UploadUtils.unbindFromService(mUploadServiceToken);
            mUploadServiceToken = null;
        }
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
        } catch (Exception ignored){

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

    private void attachFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }

        //transaction.commit();

        // Need to test this!!
        transaction.commitAllowingStateLoss();
    }

    @Override
    public void setStatusbarColored(boolean colored, boolean invertIcons) {
        int statusbarNonColored = CurrentTheme.getStatusBarNonColored(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View fakeStatusBarView = findViewById(R.id.fake_statusbar);
            if (fakeStatusBarView != null) {
                int colorPrimaryDark = CurrentTheme.getColorPrimaryDark(this);
                fakeStatusBarView.setBackgroundColor(colored ? colorPrimaryDark : statusbarNonColored);
                ViewGroup.LayoutParams layoutParams = fakeStatusBarView.getLayoutParams();
                layoutParams.height = Utils.getStatusBarHeight(this);
                fakeStatusBarView.setLayoutParams(layoutParams);
            }
        }

        if (Utils.hasLollipop()) {
            Window w = getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(colored ? Color.TRANSPARENT : statusbarNonColored);

            if (Settings.get().ui().isNavigationbarColored()) {
                @ColorInt
                int navigationColor = colored ? CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
                w.setNavigationBarColor(navigationColor);
            }
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
                attachFragment(VideoPreviewFragment.newInstance(place.getArgs()), true, "video_preview");
                break;

            case Place.FRIENDS_AND_FOLLOWERS:
                attachFragment(FriendsTabsFragment.newInstance(place.getArgs()), true, "friends");
                break;

            case Place.WIKI_PAGE:
                attachFragment(BrowserFragment.newInstance(place.getArgs()), true, "wikipage");
                break;

            case Place.EXTERNAL_LINK:
                attachFragment(BrowserFragment.newInstance(place.getArgs()), true, "unknown_link");
                break;

            case Place.DOC_PREVIEW:
                Document document = place.getArgs().getParcelable(Extra.DOC);
                if (document != null && document.hasValidGifVideoLink()) {
                    int aid = place.getArgs().getInt(Extra.ACCOUNT_ID);
                    ArrayList<Document> documents = new ArrayList<>(Collections.singletonList(document));

                    Bundle argsForGifs = GifPagerFragment.buildArgs(aid, documents, 0);
                    attachFragment(GifPagerFragment.newInstance(argsForGifs), true, "gif_player");
                } else {
                    attachFragment(DocPreviewFragment.newInstance(place.getArgs()), true, "doc_preview");
                }
                break;

            case Place.WALL_POST:
                attachFragment(WallPostFragment.newInstance(place.getArgs()), true, "post");
                break;

            case Place.COMMENTS:
                attachFragment(CommentsFragment.newInstance(place), true, "comments");
                break;

            case Place.WALL:
                attachFragment(AbsWallFragment.newInstance(place.getArgs()), true, "owner-wall");
                break;

            case Place.CONVERSATION_ATTACHMENTS:
                attachFragment(ConversationFragmentFactory.newInstance(place.getArgs()), true, "conversation_attachments");
                break;

            case Place.PLAYER:
                if (!(getFrontFragement() instanceof AudioPlayerFragment)) {
                    attachFragment(AudioPlayerFragment.newInstance(place.getArgs()), true, "player");
                }
                break;

            case Place.CHAT:
                final Peer peer = place.getArgs().getParcelable(Extra.PEER);
                AssertUtils.requireNonNull(peer);
                openChat(place.getArgs().getInt(Extra.ACCOUNT_ID), place.getArgs().getInt(Extra.OWNER_ID), peer);
                break;

            case Place.SEARCH:
                attachFragment(SeachTabsFragment.newInstance(place.getArgs()), true, "search");
                break;

            case Place.BUILD_NEW_POST:
                PostCreateFragment postCreateFragment = PostCreateFragment.newInstance(place.getArgs());
                place.applyTargetingTo(postCreateFragment);
                attachFragment(postCreateFragment, true, "create_post");
                break;

            case Place.EDIT_COMMENT:
                Comment comment = place.getArgs().getParcelable(Extra.COMMENT);
                int accountId = place.getArgs().getInt(Extra.ACCOUNT_ID);
                CommentEditFragment commentEditFragment = CommentEditFragment.newInstance(accountId, comment);
                place.applyTargetingTo(commentEditFragment);
                attachFragment(commentEditFragment, true, "edit_comment");
                break;

            case Place.EDIT_POST:
                PostEditFragment postEditFragment = PostEditFragment.newInstance(place.getArgs());
                place.applyTargetingTo(postEditFragment);
                attachFragment(postEditFragment, true, "edit_post");
                break;

            case Place.REPOST:
                attachFragment(RepostFragment.obtain(place), true, "repost");
                break;

            case Place.DIALOGS:
                attachFragment(DialogsFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getInt(Extra.OWNER_ID),
                        place.getArgs().getString(Extra.SUBTITLE)
                ), true, "dialogs");
                break;

            case Place.FORWARD_MESSAGES:
                attachFragment(FwdsFragment.newInstance(place.getArgs()), true, "fwds");
                break;

            case Place.TOPICS:
                attachFragment(TopicsFragment.newInstance(place.getArgs()), true, "topics");
                break;

            case Place.CHAT_MEMBERS:
                attachFragment(ChatUsersFragment.newInstance(place.getArgs()), true, "chat_members");
                break;

            case Place.COMMUNITIES:
                CommunitiesFragment communitiesFragment = CommunitiesFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.USER_ID)
                );

                attachFragment(communitiesFragment, true, "communities");
                break;

            case Place.AUDIOS:
                attachFragment(AudiosFragment.newInstance(args.getInt(Extra.ACCOUNT_ID)), true, "audios");
                break;

            case Place.VIDEO_ALBUM:
                attachFragment(VideosFragment.newInstance(place.getArgs()), true, "video_album");
                break;

            case Place.VIDEOS:
                attachFragment(VideosTabsFragment.newInstance(place.getArgs()), true, "videos");
                break;

            case Place.VK_PHOTO_ALBUMS:
                attachFragment(VKPhotoAlbumsFragment.newInstance(
                        args.getInt(Extra.ACCOUNT_ID),
                        args.getInt(Extra.OWNER_ID),
                        args.getString(Extra.ACTION),
                        args.getParcelable(Extra.OWNER)
                ), true, "vk_photo_almums");
                break;

            case Place.VK_PHOTO_ALBUM:
                attachFragment(VKPhotosFragment.newInstance(place.getArgs()), true, "vk_photos_album");
                break;

            case Place.VK_PHOTO_ALBUM_GALLERY:
                //attachFragment(AlbumPhotoPagerFragment.newInstance(place.getArgs()), true, "vk_album_gallery");
                attachFragment(PhotoPagerFragment.newInstance(place.type, place.getArgs()), true, "photo_pager");
                break;

            case Place.FAVE_PHOTOS_GALLERY:
                attachFragment(PhotoPagerFragment.newInstance(place.type, place.getArgs()), true, "fave_photos_gallery");
                break;

            case Place.SIMPLE_PHOTO_GALLERY:
                attachFragment(PhotoPagerFragment.newInstance(place.type, place.getArgs()), true, "photo_pager");
                break;

            case Place.VK_PHOTO_TMP_SOURCE:
                attachFragment(PhotoPagerFragment.newInstance(place.type, place.getArgs()), true, "tmp_source_gallery");
                break;

            case Place.POLL:
                attachFragment(PollFragment.newInstance(place.getArgs()), true, "poll");
                break;

            case Place.BOOKMARKS:
                attachFragment(FaveTabsFragment.newInstance(place.getArgs()), true, "fave_tabs");
                break;

            case Place.DOCS:
                attachFragment(DocsFragment.newInstance(place.getArgs()), true, "docs");
                break;

            case Place.FEED:
                attachFragment(FeedFragment.newInstance(place.getArgs()), true, "feed");
                break;

            case Place.NOTIFICATIONS:
                attachFragment(FeedbackFragment.newInstance(place.getArgs()), true, "notifications");
                break;

            case Place.PREFERENCES:
                attachFragment(PreferencesFragment.newInstance(place.getArgs()), true, "preferences");
                break;

            case Place.RESOLVE_DOMAIN:
                ResolveDomainDialog domainDialog = ResolveDomainDialog.newInstance(place.getArgs());
                domainDialog.show(getSupportFragmentManager(), "resolve_domain");
                break;

            case Place.VK_INTERNAL_PLAYER:
                Intent intent = new Intent(this, VideoPlayerActivity.class);
                intent.putExtras(place.getArgs());
                startActivity(intent);
                break;

            case Place.NOTIFICATION_SETTINGS:
                attachFragment(new NotificationPreferencesFragment(), true, "notifications");
                break;

            case Place.LIKES_AND_COPIES:
                attachFragment(LikesFragment.newInstance(place.getArgs()), true, "likes");
                break;

            case Place.CREATE_PHOTO_ALBUM:
            case Place.EDIT_PHOTO_ALBUM:
                CreatePhotoAlbumFragment createPhotoAlbumFragment = CreatePhotoAlbumFragment.newInstance(place.getArgs());
                place.applyTargetingTo(createPhotoAlbumFragment);
                attachFragment(createPhotoAlbumFragment, true, "create_or_edit_photo_album");
                break;

            case Place.MESSAGE_LOOKUP:
                attachFragment(MessagesLookFragment.newInstance(place.getArgs()), true, "messages_lookup");
                break;

            case Place.AUDIO_CURRENT_PLAYLIST:
                attachFragment(PlaylistFragment.newInstance(place.getArgs()), true, "audio_playlist");
                break;

            case Place.GIF_PAGER:
                attachFragment(GifPagerFragment.newInstance(place.getArgs()), true, "gif_pager");
                break;

            case Place.SECURITY:
                attachFragment(new SecurityPreferencesFragment(), true, "security");
                break;

            case Place.CREATE_POLL:
                CreatePollFragment createPollFragment = CreatePollFragment.newInstance(place.getArgs());
                place.applyTargetingTo(createPollFragment);
                attachFragment(createPollFragment, true, "create_poll");
                break;

            case Place.COMMENT_CREATE:
                openCommentCreatePlace(place);
                break;

            case Place.LOGS:
                attachFragment(LogsFragement.newInstance(), true, "logs");
                break;

            case Place.SINGLE_SEARCH:
                SingleTabSeachFragment singleTabSeachFragment = SingleTabSeachFragment.newInstance(place.getArgs());
                attachFragment(singleTabSeachFragment, true, "single_tab_fragment");
                break;

            case Place.NEWSFEED_COMMENTS:
                NewsfeedCommentsFragment newsfeedCommentsFragment = NewsfeedCommentsFragment.newInstance(place.getArgs().getInt(Extra.ACCOUNT_ID));
                attachFragment(newsfeedCommentsFragment, true, "newsfeed_comments");
                break;

            case Place.COMMUNITY_CONTROL:
                CommunityControlFragment communityControlFragment = CommunityControlFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getParcelable(Extra.OWNER),
                        place.getArgs().getParcelable(Extra.SETTINGS)
                );
                attachFragment(communityControlFragment, true, "community_control");
                break;

            case Place.COMMUNITY_BAN_EDIT:
                CommunityBanEditFragment communityBanEditFragment = CommunityBanEditFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getInt(Extra.GROUP_ID),
                        (Banned) place.getArgs().getParcelable(Extra.BANNED)
                );
                attachFragment(communityBanEditFragment, true, "community_ban_edit");
                break;

            case Place.COMMUNITY_ADD_BAN:
                attachFragment(CommunityBanEditFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getInt(Extra.GROUP_ID),
                        place.getArgs().getParcelableArrayList(Extra.USERS)
                ), true, "community_ban_add");
                break;

            case Place.COMMUNITY_MANAGER_ADD:
                attachFragment(CommunityManagerEditFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getInt(Extra.GROUP_ID),
                        place.getArgs().getParcelableArrayList(Extra.USERS)
                ), true, "managers_adding");
                break;

            case Place.COMMUNITY_MANAGER_EDIT:
                attachFragment(CommunityManagerEditFragment.newInstance(
                        place.getArgs().getInt(Extra.ACCOUNT_ID),
                        place.getArgs().getInt(Extra.GROUP_ID),
                        (Manager) place.getArgs().getParcelable(Extra.MANAGER)
                ), true, "manager_editing");
                break;

            case Place.REQUEST_EXECUTOR:
                attachFragment(RequestExecuteFragment.newInstance(place.getArgs().getInt(Extra.ACCOUNT_ID)), true, "request-executor");
                break;

            case Place.USER_BLACKLIST:
                attachFragment(UserBannedFragment.newInstance(place.getArgs().getInt(Extra.ACCOUNT_ID)), true, "user-blacklist");
                break;

            case Place.DRAWER_EDIT:
                attachFragment(DrawerEditFragment.newInstance(), true, "drawer-edit");
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
        attachFragment(fragment, true, "comemnt_create");
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

        if (name.getClassName().equals(UploadService.class.getName())) {
            Logger.d(TAG, "Disconnected from UploadService");
            mUploadServiceToken = null;
            bindToUploadService();
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
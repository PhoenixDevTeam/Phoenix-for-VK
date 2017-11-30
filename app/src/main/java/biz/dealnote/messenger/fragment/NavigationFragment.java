package biz.dealnote.messenger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.MenuListAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.model.Sex;
import biz.dealnote.messenger.model.SwitchableCategory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.drawer.AbsDrawerItem;
import biz.dealnote.messenger.model.drawer.DividerDrawerItem;
import biz.dealnote.messenger.model.drawer.IconDrawerItem;
import biz.dealnote.messenger.model.drawer.NoIconDrawerItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.model.drawer.SectionDrawerItem;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.AppPrefs;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RoundTransformation;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.model.SwitchableCategory.BOOKMARKS;
import static biz.dealnote.messenger.model.SwitchableCategory.DIALOGS;
import static biz.dealnote.messenger.model.SwitchableCategory.DOCS;
import static biz.dealnote.messenger.model.SwitchableCategory.FEED;
import static biz.dealnote.messenger.model.SwitchableCategory.FEEDBACK;
import static biz.dealnote.messenger.model.SwitchableCategory.FRIENDS;
import static biz.dealnote.messenger.model.SwitchableCategory.GROUPS;
import static biz.dealnote.messenger.model.SwitchableCategory.MUSIC;
import static biz.dealnote.messenger.model.SwitchableCategory.NEWSFEED_COMMENTS;
import static biz.dealnote.messenger.model.SwitchableCategory.PHOTOS;
import static biz.dealnote.messenger.model.SwitchableCategory.SEARCH;
import static biz.dealnote.messenger.model.SwitchableCategory.VIDEOS;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class NavigationFragment extends BaseFragment {

    private static final String TAG = NavigationFragment.class.getSimpleName();

    public static final int PAGE_FRIENDS = 0;
    public static final int PAGE_DIALOGS = 1;
    public static final int PAGE_FEED = 7;
    public static final int PAGE_MUSIC = 2;
    public static final int PAGE_DOCUMENTS = 3;
    public static final int PAGE_PHOTOS = 6;
    public static final int PAGE_PREFERENSES = 4;
    public static final int PAGE_ACCOUNTS = 5;
    public static final int PAGE_GROUPS = 8;
    public static final int PAGE_VIDEOS = 9;
    public static final int PAGE_BOOKMARKS = 10;
    public static final int PAGE_BUY_FULL_APP = 11;
    public static final int PAGE_NOTIFICATION = 12;
    public static final int PAGE_SEARCH = 13;
    public static final int PAGE_NEWSFEED_COMMENTS = 14;

    public static final SectionDrawerItem SECTION_ITEM_FRIENDS = new IconDrawerItem(PAGE_FRIENDS, R.drawable.human_child, R.string.friends);
    public static final SectionDrawerItem SECTION_ITEM_DIALOGS = new IconDrawerItem(PAGE_DIALOGS, R.drawable.email, R.string.dialogs);
    public static final SectionDrawerItem SECTION_ITEM_FEED = new IconDrawerItem(PAGE_FEED, R.drawable.rss, R.string.feed);
    public static final SectionDrawerItem SECTION_ITEM_FEEDBACK = new IconDrawerItem(PAGE_NOTIFICATION, R.drawable.heart, R.string.drawer_feedback);
    public static final SectionDrawerItem SECTION_ITEM_NEWSFEED_COMMENTS = new IconDrawerItem(PAGE_NEWSFEED_COMMENTS, R.drawable.ic_drawer_newsfeed_comment, R.string.drawer_newsfeed_comments);
    public static final SectionDrawerItem SECTION_ITEM_GROUPS = new IconDrawerItem(PAGE_GROUPS, R.drawable.google_circles, R.string.groups);
    public static final SectionDrawerItem SECTION_ITEM_PHOTOS = new IconDrawerItem(PAGE_PHOTOS, R.drawable.camera, R.string.photos);
    public static final SectionDrawerItem SECTION_ITEM_VIDEOS = new IconDrawerItem(PAGE_VIDEOS, R.drawable.video, R.string.videos);
    public static final SectionDrawerItem SECTION_ITEM_BOOKMARKS = new IconDrawerItem(PAGE_BOOKMARKS, R.drawable.star, R.string.bookmarks);
    public static final SectionDrawerItem SECTION_ITEM_AUDIOS = new IconDrawerItem(PAGE_MUSIC, R.drawable.music_circle, R.string.music);
    public static final SectionDrawerItem SECTION_ITEM_DOCS = new IconDrawerItem(PAGE_DOCUMENTS, R.drawable.file, R.string.attachment_documents);
    public static final SectionDrawerItem SECTION_ITEM_SEARCH = new IconDrawerItem(PAGE_SEARCH, R.drawable.magnify, R.string.search);

    public static final SectionDrawerItem SECTION_ITEM_SETTINGS = new NoIconDrawerItem(PAGE_PREFERENSES, R.string.settings);
    public static final SectionDrawerItem SECTION_ITEM_BY_FULL_APP = new NoIconDrawerItem(PAGE_BUY_FULL_APP, R.string.buy_phoenix);
    public static final SectionDrawerItem SECTION_ITEM_ACCOUNTS = new NoIconDrawerItem(PAGE_ACCOUNTS, R.string.accounts);

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final int MAX_RECENT_COUNT = 5;

    private NavigationDrawerCallbacks mCallbacks;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private ImageView ivHeaderAvatar;
    private TextView tvUserName;
    private TextView tvDomain;
    private int mCurrentSelectedPosition;
    private List<RecentChat> mRecentChats;
    private MenuListAdapter mAdapter;
    private ArrayList<AbsDrawerItem> mDrawerItems;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private int mAccountId;

    private IOwnersInteractor ownersInteractor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.ownersInteractor = InteractorFactory.createOwnerInteractor();
        this.mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        selectItem(mCurrentSelectedPosition, false);

        mRecentChats = Settings.get()
                .recentChats()
                .get(mAccountId);

        mCompositeDisposable.add(Stores.getInstance()
                .dialogs()
                .observeUnreadDialogsCount()
                .filter(pair -> pair.getFirst() == mAccountId)
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(pair -> onUnreadDialogsCountChange(pair.getSecond())));

        SECTION_ITEM_DIALOGS.setCount(Stores.getInstance()
                .dialogs()
                .getUnreadDialogsCount(mAccountId));

        mCompositeDisposable.add(Settings.get().drawerSettings()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(o -> refreshDrawerItems()));
    }

    private void onUnreadDialogsCountChange(int count) {
        Logger.d(TAG, "onUnreadDialogsCountChange, count: " + count);

        if (SECTION_ITEM_DIALOGS.getCount() != count) {
            SECTION_ITEM_DIALOGS.setCount(count);
            safellyNotifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView = root.findViewById(R.id.fragment_navigation_list);
        mDrawerListView.setOnItemClickListener((parent, view, position, id) -> selectItem(position, false));
        mDrawerListView.setOnItemLongClickListener((parent, view, position, id) -> {
            selectItem(position, true);
            return true;
        });

        View vHeader = inflater.inflate(R.layout.header_navi_menu, mDrawerListView, false);

        ImageView backgroundImage = vHeader.findViewById(R.id.header_navi_menu_background);

        File file = PreferencesFragment.getDrawerBackgroundFile(getActivity(), !Settings.get().ui().isDarkModeEnabled());
        if (file.exists()) {
            PicassoInstance.with().load(file).into(backgroundImage);
        } else {
            backgroundImage.setImageResource(R.drawable.background_drawer_header_vector);
        }

        mDrawerListView.addHeaderView(vHeader, null, false);

        ivHeaderAvatar = vHeader.findViewById(R.id.header_navi_menu_avatar);
        tvUserName = vHeader.findViewById(R.id.header_navi_menu_username);
        tvDomain = vHeader.findViewById(R.id.header_navi_menu_usernick);

        mDrawerItems = new ArrayList<>();
        mDrawerItems.addAll(generateNavDrawerItems());
        mAdapter = new MenuListAdapter(getActivity(), mDrawerItems, mDrawerListView);

        mDrawerListView.setAdapter(mAdapter);

        refreshUserInfo();

        ivHeaderAvatar.setOnClickListener(v -> {
            closeDrawer();
            openMyWall();
        });

        return root;
    }

    private void refreshUserInfo() {
        if (this.mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            mCompositeDisposable.add(ownersInteractor.getBaseOwnerInfo(mAccountId, mAccountId, IOwnersInteractor.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(owner -> refreshHeader((User) owner), t -> {/*ignored*/}));
        }
    }

    private void openMyWall() {
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        PlaceFactory.getOwnerWallPlace(mAccountId, mAccountId, null)
                .tryOpenWith(getActivity());
    }

    public void refreshDrawerItems() {
        mDrawerItems.clear();
        mDrawerItems.addAll(generateNavDrawerItems());

        safellyNotifyDataSetChanged();
    }

    private static AbsDrawerItem getItemBySwitchableCategory(@SwitchableCategory int type) {
        switch (type) {
            case FRIENDS:
                return SECTION_ITEM_FRIENDS;
            case DIALOGS:
                return SECTION_ITEM_DIALOGS;
            case FEED:
                return SECTION_ITEM_FEED;
            case FEEDBACK:
                return SECTION_ITEM_FEEDBACK;
            case NEWSFEED_COMMENTS:
                return SECTION_ITEM_NEWSFEED_COMMENTS;
            case GROUPS:
                return SECTION_ITEM_GROUPS;
            case PHOTOS:
                return SECTION_ITEM_PHOTOS;
            case VIDEOS:
                return SECTION_ITEM_VIDEOS;
            case MUSIC:
                return SECTION_ITEM_AUDIOS;
            case DOCS:
                return SECTION_ITEM_DOCS;
            case BOOKMARKS:
                return SECTION_ITEM_BOOKMARKS;
            case SEARCH:
                return SECTION_ITEM_SEARCH;
        }

        throw new UnsupportedOperationException();
    }

    private ArrayList<AbsDrawerItem> generateNavDrawerItems() {
        ISettings.IDrawerSettings settings = Settings.get().drawerSettings();

        @SwitchableCategory
        int[] categories = settings.getCategoriesOrder();

        ArrayList<AbsDrawerItem> items = new ArrayList<>();

        for (int category : categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySwitchableCategory(category));
                } catch (Exception ignored) {
                }
            }
        }

        items.add(new DividerDrawerItem());

        if (nonEmpty(mRecentChats)) {
            items.addAll(mRecentChats);
            items.add(new DividerDrawerItem());
        }

        items.add(SECTION_ITEM_SETTINGS);

        if (!AppPrefs.isFullApp()) {
            items.add(SECTION_ITEM_BY_FULL_APP);
        }

        items.add(SECTION_ITEM_ACCOUNTS);
        return items;
    }

    /**
     * Добавить новые "недавний чат" в боковую панель
     * Если там уже есть более 4-х елементов, то удаляем последний
     *
     * @param recentChat новый чат
     */
    public void appendRecentChat(@NonNull RecentChat recentChat) {
        if (mRecentChats == null) {
            mRecentChats = new ArrayList<>(1);
        }

        int index = mRecentChats.indexOf(recentChat);
        if (index != -1) {
            RecentChat old = mRecentChats.get(index);

            // если вдруг мы дабавляем чат без иконки или названия, то сохраним эти
            // значения из пердыдущего (c тем же peer_id) елемента
            recentChat.setIconUrl(Utils.firstNonEmptyString(recentChat.getIconUrl(), old.getIconUrl()));
            recentChat.setTitle(Utils.firstNonEmptyString(recentChat.getTitle(), old.getTitle()));

            mRecentChats.set(index, recentChat);
        } else {
            if (mRecentChats.size() >= MAX_RECENT_COUNT) {
                mRecentChats.remove(mRecentChats.size() - 1);
            }

            mRecentChats.add(0, recentChat);
        }

        refreshDrawerItems();
    }

    private void refreshHeader(User user) {
        if (!isAdded()) return;

        String avaUrl = user.getMaxSquareAvatar();
        String fullName = user.getFullName();

        Transformation transformation = new RoundTransformation();
        if (nonNull(avaUrl) && !avaUrl.contains("camera")) {
            PicassoInstance.with()
                    .load(avaUrl)
                    .transform(transformation)
                    .into(ivHeaderAvatar);
        } else {
            PicassoInstance.with()
                    .load(user.getSex() == Sex.WOMAN ? R.drawable.ic_sex_woman : R.drawable.ic_sex_man)
                    .transform(transformation)
                    .into(ivHeaderAvatar);
        }

        tvUserName.setText(fullName);

        if (tvDomain != null) {
            String domailText = "@" + user.getDomain();
            tvDomain.setText(domailText);
        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void openDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        if (drawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }
    }

    private void selectItem(int position, boolean longClick) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        if (mCallbacks != null && mAdapter != null) {
            AbsDrawerItem item = mAdapter.getItem(position - mDrawerListView.getHeaderViewsCount());
            mCallbacks.onNavigationDrawerItemSelected(item, longClick);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (NavigationDrawerCallbacks) context;
        } catch (ClassCastException ignored) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    public void selectPage(AbsDrawerItem item) {
        int index = item == null ? -1 : mDrawerItems.indexOf(item);
        if (index != -1) {
            int targetIndex = index + mDrawerListView.getHeaderViewsCount();
            mDrawerListView.setItemChecked(targetIndex, true);
        } else {
            mDrawerListView.clearChoices();
        }

        mAdapter.notifyDataSetChanged();
    }

    private void backupRecentChats() {
        List<RecentChat> chats = new ArrayList<>(5);
        for (AbsDrawerItem item : mDrawerItems) {
            if (item instanceof RecentChat) {
                chats.add((RecentChat) item);
            }
        }

        Settings.get()
                .recentChats()
                .store(mAccountId, chats);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        backupRecentChats();
        super.onDestroy();
    }

    private void onAccountChange(int newAccountId) {
        backupRecentChats();

        mAccountId = newAccountId;
        SECTION_ITEM_DIALOGS.setCount(Stores.getInstance()
                .dialogs()
                .getUnreadDialogsCount(mAccountId));

        mRecentChats = Settings.get()
                .recentChats()
                .get(mAccountId);

        refreshDrawerItems();

        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            refreshUserInfo();
        }
    }

    private void safellyNotifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            try {
                mAdapter.notifyDataSetChanged();
            } catch (Exception ignored) {
            }
        }
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(AbsDrawerItem item, boolean longClick);
    }
}
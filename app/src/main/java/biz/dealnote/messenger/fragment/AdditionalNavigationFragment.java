package biz.dealnote.messenger.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.MenuListAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.model.Sex;
import biz.dealnote.messenger.model.SwitchableCategory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.drawer.AbsMenuItem;
import biz.dealnote.messenger.model.drawer.IconMenuItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.model.drawer.SectionMenuItem;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.AppPrefs;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.RoundTransformation;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.model.SwitchableCategory.BOOKMARKS;
import static biz.dealnote.messenger.model.SwitchableCategory.DOCS;
import static biz.dealnote.messenger.model.SwitchableCategory.FRIENDS;
import static biz.dealnote.messenger.model.SwitchableCategory.GROUPS;
import static biz.dealnote.messenger.model.SwitchableCategory.MUSIC;
import static biz.dealnote.messenger.model.SwitchableCategory.NEWSFEED_COMMENTS;
import static biz.dealnote.messenger.model.SwitchableCategory.PHOTOS;
import static biz.dealnote.messenger.model.SwitchableCategory.VIDEOS;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class AdditionalNavigationFragment extends BaseFragment implements MenuListAdapter.ActionListener {

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

    public static final SectionMenuItem SECTION_ITEM_FRIENDS = new IconMenuItem(PAGE_FRIENDS, R.drawable.friends, R.string.friends);
    public static final SectionMenuItem SECTION_ITEM_DIALOGS = new IconMenuItem(PAGE_DIALOGS, R.drawable.email, R.string.dialogs);
    public static final SectionMenuItem SECTION_ITEM_FEED = new IconMenuItem(PAGE_FEED, R.drawable.rss, R.string.feed);
    public static final SectionMenuItem SECTION_ITEM_FEEDBACK = new IconMenuItem(PAGE_NOTIFICATION, R.drawable.heart, R.string.drawer_feedback);
    public static final SectionMenuItem SECTION_ITEM_NEWSFEED_COMMENTS = new IconMenuItem(PAGE_NEWSFEED_COMMENTS, R.drawable.comment, R.string.drawer_newsfeed_comments);
    public static final SectionMenuItem SECTION_ITEM_GROUPS = new IconMenuItem(PAGE_GROUPS, R.drawable.google_circles, R.string.groups);
    public static final SectionMenuItem SECTION_ITEM_PHOTOS = new IconMenuItem(PAGE_PHOTOS, R.drawable.camera, R.string.photos);
    public static final SectionMenuItem SECTION_ITEM_VIDEOS = new IconMenuItem(PAGE_VIDEOS, R.drawable.video, R.string.videos);
    public static final SectionMenuItem SECTION_ITEM_BOOKMARKS = new IconMenuItem(PAGE_BOOKMARKS, R.drawable.star, R.string.bookmarks);
    public static final SectionMenuItem SECTION_ITEM_AUDIOS = new IconMenuItem(PAGE_MUSIC, R.drawable.music, R.string.music);
    public static final SectionMenuItem SECTION_ITEM_DOCS = new IconMenuItem(PAGE_DOCUMENTS, R.drawable.file, R.string.attachment_documents);
    public static final SectionMenuItem SECTION_ITEM_SEARCH = new IconMenuItem(PAGE_SEARCH, R.drawable.magnify, R.string.search);

    public static final SectionMenuItem SECTION_ITEM_SETTINGS = new IconMenuItem(PAGE_PREFERENSES, R.drawable.settings, R.string.settings);
    public static final SectionMenuItem SECTION_ITEM_BY_FULL_APP = new IconMenuItem(PAGE_BUY_FULL_APP, R.drawable.phoenix, R.string.buy_phoenix);
    public static final SectionMenuItem SECTION_ITEM_ACCOUNTS = new IconMenuItem(PAGE_ACCOUNTS, R.drawable.account_circle, R.string.accounts);

    private static final int MAX_RECENT_COUNT = 5;

    private NavigationDrawerCallbacks mCallbacks;
    private BottomSheetBehavior mBottomSheetBehavior;

    private ViewGroup vgProfileContainer;
    private ImageView ivHeaderAvatar;
    private TextView tvUserName;
    private TextView tvDomain;

    private List<RecentChat> mRecentChats;
    private MenuListAdapter mAdapter;
    private List<AbsMenuItem> mDrawerItems;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private int mAccountId;

    private IOwnersRepository ownersRepository;

    private static AbsMenuItem getItemBySwitchableCategory(@SwitchableCategory int type) {
        switch (type) {
            case FRIENDS:
                return SECTION_ITEM_FRIENDS;
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
        }

        throw new UnsupportedOperationException();
    }

    private void onUnreadDialogsCountChange(int count) {
        if (SECTION_ITEM_DIALOGS.getCount() != count) {
            SECTION_ITEM_DIALOGS.setCount(count);
            safellyNotifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ownersRepository = Repository.INSTANCE.getOwners();

        mAccountId = Settings.get()
                .accounts()
                .getCurrent();

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));

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
                .subscribe(o -> refreshNavigationItems()));
    }

    private void refreshUserInfo() {
        if (mAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            mCompositeDisposable.add(ownersRepository.getBaseOwnerInfo(mAccountId, mAccountId, IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(owner -> refreshHeader((User) owner), ignore()));
        }
    }

    private void openMyWall() {
        if (mAccountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }
        PlaceFactory.getOwnerWallPlace(mAccountId, mAccountId, null).tryOpenWith(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));

        vgProfileContainer = root.findViewById(R.id.content_root);
        ivHeaderAvatar = root.findViewById(R.id.header_navi_menu_avatar);
        tvUserName = root.findViewById(R.id.header_navi_menu_username);
        tvDomain = root.findViewById(R.id.header_navi_menu_usernick);

        mDrawerItems = new ArrayList<>();
        mDrawerItems.addAll(generateNavDrawerItems());

        mAdapter = new MenuListAdapter(requireActivity(), mDrawerItems, this);

        mBottomSheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bottom_sheet));
        closeSheet();

        recyclerView.setAdapter(mAdapter);

        refreshUserInfo();

        vgProfileContainer.setOnClickListener(v -> {
            closeSheet();
            openMyWall();
        });

        return root;
    }

    public void refreshNavigationItems() {
        mDrawerItems.clear();
        mDrawerItems.addAll(generateNavDrawerItems());

        safellyNotifyDataSetChanged();
    }

    private ArrayList<AbsMenuItem> generateNavDrawerItems() {
        ISettings.IDrawerSettings settings = Settings.get().drawerSettings();

        @SwitchableCategory
        int[] categories = settings.getCategoriesOrder();

        ArrayList<AbsMenuItem> items = new ArrayList<>();

        for (int category : categories) {
            if (settings.isCategoryEnabled(category)) {
                try {
                    items.add(getItemBySwitchableCategory(category));
                } catch (Exception ignored) {
                }
            }
        }

//        items.add(new DividerMenuItem());

        items.add(SECTION_ITEM_SETTINGS);

        if (!AppPrefs.isFullApp()) {
            items.add(SECTION_ITEM_BY_FULL_APP);
        }

        items.add(SECTION_ITEM_ACCOUNTS);

        if (nonEmpty(mRecentChats)) {
            items.addAll(mRecentChats);
        }
        return items;
    }

    /**
     * Добавить новый "недавний чат" в боковую панель
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
            recentChat.setIconUrl(firstNonEmptyString(recentChat.getIconUrl(), old.getIconUrl()));
            recentChat.setTitle(firstNonEmptyString(recentChat.getTitle(), old.getTitle()));

            mRecentChats.set(index, recentChat);
        } else {
            if (mRecentChats.size() >= MAX_RECENT_COUNT) {
                mRecentChats.remove(mRecentChats.size() - 1);
            }

            mRecentChats.add(0, recentChat);
        }

        refreshNavigationItems();
    }

    private void refreshHeader(User user) {
        if (!isAdded()) return;

        String avaUrl = user.getMaxSquareAvatar();

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

        String domailText = "@" + user.getDomain();
        tvDomain.setText(domailText);
        tvUserName.setText(user.getFullName());
    }

    public boolean isSheetOpen() {
        return mBottomSheetBehavior != null && mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED;
    }

    public void openSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void closeSheet() {
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void selectItem(AbsMenuItem item, boolean longClick) {
        closeSheet();

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(item, longClick);
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
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

    public void selectPage(AbsMenuItem item) {
        for (AbsMenuItem i : mDrawerItems) {
            i.setSelected(i == item);
        }
        safellyNotifyDataSetChanged();
    }

    private void backupRecentChats() {
        List<RecentChat> chats = new ArrayList<>(5);
        for (AbsMenuItem item : mDrawerItems) {
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
//        SECTION_ITEM_DIALOGS.setCount(Stores.getInstance()
//                .dialogs()
//                .getUnreadDialogsCount(mAccountId));

        mRecentChats = Settings.get()
                .recentChats()
                .get(mAccountId);

        refreshNavigationItems();

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

    @Override
    public void onDrawerItemClick(AbsMenuItem item) {
        selectItem(item, false);
    }

    @Override
    public void onDrawerItemLongClick(AbsMenuItem item) {
        selectItem(item, true);
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(AbsMenuItem item, boolean longClick);
    }
}
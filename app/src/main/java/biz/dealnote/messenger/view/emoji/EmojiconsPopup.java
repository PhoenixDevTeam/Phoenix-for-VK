package biz.dealnote.messenger.view.emoji;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.model.StickerSet;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.view.emoji.section.Cars;
import biz.dealnote.messenger.view.emoji.section.Electronics;
import biz.dealnote.messenger.view.emoji.section.Emojicon;
import biz.dealnote.messenger.view.emoji.section.Food;
import biz.dealnote.messenger.view.emoji.section.Nature;
import biz.dealnote.messenger.view.emoji.section.People;
import biz.dealnote.messenger.view.emoji.section.Sport;
import biz.dealnote.messenger.view.emoji.section.Symbols;

public class EmojiconsPopup {

    private static final String KEY_PAGE = "emoji_page";

    private EmojisPagerAdapter mEmojisAdapter;
    private int keyBoardHeight;

    private boolean isOpened;

    private EmojiconGridView.OnEmojiconClickedListener onEmojiconClickedListener;
    private StickersGridView.OnStickerClickedListener onStickerClickedListener;
    private OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    private OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;

    private View rootView;
    private View emojiContainer;
    private Activity mContext;
    private ViewPager emojisPager;

    public EmojiconsPopup(View rootView, Activity context) {
        this.mContext = context;
        this.rootView = rootView;
        listenKeyboardSize();
    }

    private void storeState() {
        if (Objects.nonNull(emojisPager)) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putInt(KEY_PAGE, emojisPager.getCurrentItem())
                    .apply();
        }
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        this.onSoftKeyboardOpenCloseListener = listener;
    }

    public void setOnEmojiconClickedListener(EmojiconGridView.OnEmojiconClickedListener listener) {
        this.onEmojiconClickedListener = listener;
    }

    public void setOnStickerClickedListener(StickersGridView.OnStickerClickedListener onStickerClickedListener) {
        this.onStickerClickedListener = onStickerClickedListener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        this.onEmojiconBackspaceClickedListener = listener;
    }

    public EmojiconGridView.OnEmojiconClickedListener getOnEmojiconClickedListener() {
        return onEmojiconClickedListener;
    }

    public StickersGridView.OnStickerClickedListener getOnStickerClickedListener() {
        return onStickerClickedListener;
    }

    public OnEmojiconBackspaceClickedListener getOnEmojiconBackspaceClickedListener() {
        return onEmojiconBackspaceClickedListener;
    }

    public OnSoftKeyboardOpenCloseListener getOnSoftKeyboardOpenCloseListener() {
        return onSoftKeyboardOpenCloseListener;
    }

    public boolean isKeyBoardOpen() {
        return isOpened;
    }

    private OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootView.getRootView().getHeight();
            int heightDifference = screenHeight - (r.bottom - r.top);

            int navBarHeight = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

            if (navBarHeight > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(navBarHeight);
            }

            int statusbarHeight = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusbarHeight > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(statusbarHeight);
            }

            if (heightDifference > 200) {
                keyBoardHeight = heightDifference;

                if (Objects.nonNull(emojiContainer)) {
                    emojiContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, keyBoardHeight));
                }

                if (!isOpened) {
                    if (onSoftKeyboardOpenCloseListener != null) {
                        onSoftKeyboardOpenCloseListener.onKeyboardOpen();
                    }
                }
                isOpened = true;
            } else {
                isOpened = false;
                if (onSoftKeyboardOpenCloseListener != null) {
                    onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        }
    };

    private void listenKeyboardSize() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public View getEmojiView(ViewGroup emojiParentView) {
        if (Objects.isNull(emojiContainer)) {
            emojiContainer = createCustomView(emojiParentView);

            final int finalKeyboardHeight = this.keyBoardHeight > 0 ? keyBoardHeight : (int) mContext.getResources().getDimension(R.dimen.keyboard_height);
            emojiContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, finalKeyboardHeight));
        }

        return emojiContainer;
    }

    private View createCustomView(ViewGroup parent) {
        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        List<StickerSet> stickerSets = Stores.getInstance()
                .stickers()
                .getPurchasedAndActive(accountId)
                .blockingGet();

        View view = LayoutInflater.from(mContext).inflate(R.layout.emojicons, parent, false);
        emojisPager = view.findViewById(R.id.emojis_pager);

        List<EmojiconGridView> views = Arrays.asList(
                new EmojiconGridView(mContext, People.DATA, this),
                new EmojiconGridView(mContext, Nature.DATA, this),
                new EmojiconGridView(mContext, Food.DATA, this),
                new EmojiconGridView(mContext, Sport.DATA, this),
                new EmojiconGridView(mContext, Cars.DATA, this),
                new EmojiconGridView(mContext, Electronics.DATA, this),
                new EmojiconGridView(mContext, Symbols.DATA, this)
        );

        final List<AbsSection> sections = new ArrayList<>();
        sections.add(new EmojiSection(EmojiSection.TYPE_PEOPLE, getTintedDrawable(R.drawable.ic_emoji_people_vector)));
        sections.add(new EmojiSection(EmojiSection.TYPE_NATURE, getTintedDrawable(R.drawable.pine_tree)));
        sections.add(new EmojiSection(EmojiSection.TYPE_FOOD, getTintedDrawable(R.drawable.pizza)));
        sections.add(new EmojiSection(EmojiSection.TYPE_SPORT, getTintedDrawable(R.drawable.bike)));
        sections.add(new EmojiSection(EmojiSection.TYPE_CARS, getTintedDrawable(R.drawable.car)));
        sections.add(new EmojiSection(EmojiSection.TYPE_ELECTRONICS, getTintedDrawable(R.drawable.laptop_chromebook)));
        sections.add(new EmojiSection(EmojiSection.TYPE_SYMBOLS, getTintedDrawable(R.drawable.pound_box)));

        List<StickersGridView> stickersGridViews = new ArrayList<>();
        for (StickerSet stickerSet : stickerSets) {
            stickersGridViews.add(new StickersGridView(mContext, stickerSet, this));
            sections.add(new StickerSection(stickerSet));
        }


        mEmojisAdapter = new EmojisPagerAdapter(views, stickersGridViews);
        emojisPager.setAdapter(mEmojisAdapter);

        int storedPage = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(KEY_PAGE, 0);
        if (mEmojisAdapter.getCount() > storedPage) {
            emojisPager.setCurrentItem(storedPage);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycleView);
        LinearLayoutManager manager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);

        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);

        sections.get(emojisPager.getCurrentItem()).active = true;

        final SectionsAdapter topSectionAdapter = new SectionsAdapter(sections, mContext);
        recyclerView.setAdapter(topSectionAdapter);

        view.findViewById(R.id.backspase).setOnTouchListener(new RepeatListener(700, 50, v -> {
            if (onEmojiconBackspaceClickedListener != null) {
                onEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
            }
        }));

        topSectionAdapter.setListener(position -> emojisPager.setCurrentItem(position));

        emojisPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int oldSelectionIndex = -1;
                for (int i = 0; i < sections.size(); i++) {
                    AbsSection section = sections.get(i);

                    if (section.active) {
                        oldSelectionIndex = i;
                    }

                    section.active = position == i;
                }

                topSectionAdapter.notifyItemChanged(position);
                if (oldSelectionIndex != -1) {
                    topSectionAdapter.notifyItemChanged(oldSelectionIndex);
                }

                manager.scrollToPosition(position);
            }
        });

        return view;
    }

    private Drawable getTintedDrawable(int resourceID) {
        Drawable drawable = ContextCompat.getDrawable(mContext, resourceID);
        drawable.setColorFilter(CurrentTheme.getIconColorStatic(mContext), PorterDuff.Mode.MULTIPLY);
        return drawable;
    }

    public static void input(EditText editText, Emojicon emojicon) {
        if (editText == null || emojicon == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojicon.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    public void destroy() {
        storeState();

        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        rootView = null;
    }

    private static class EmojisPagerAdapter extends PagerAdapter {

        private List<EmojiconGridView> views;
        private List<StickersGridView> stickersGridViews;

        EmojisPagerAdapter(List<EmojiconGridView> views, List<StickersGridView> stickersGridViews) {
            super();
            this.views = views;
            this.stickersGridViews = stickersGridViews;
        }

        @Override
        public int getCount() {
            return views.size() + stickersGridViews.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    View v = views.get(position).rootView;
                    container.addView(v, 0);
                    return v;
                default:
                    View sView = stickersGridViews.get(position - 7).rootView;
                    container.addView(sView, 0);
                    return sView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object key) {
            return key == view;
        }
    }

    /**
     * A class, that can be used as a TouchListener on any view (e.g. a Button).
     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
     * click is fired immediately, next before initialInterval, and subsequent before
     * normalInterval.
     * <p/>
     * <p>Interval is scheduled before the onClick completes, so it has to run fast.
     * If it runs slow, it does not generate skipped onClicks.
     */
    public static class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final OnClickListener clickListener;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        private View downView;

        /**
         * @param initialInterval The interval before first click event
         * @param normalInterval  The interval before second and subsequent click
         *                        events
         * @param clickListener   The OnMessageActionListener, that will be called
         *                        periodically
         */
        public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen();

        void onKeyboardClose();
    }
}
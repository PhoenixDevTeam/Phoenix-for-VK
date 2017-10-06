package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.DrawerCategory;
import biz.dealnote.messenger.model.SwitchableCategory;
import biz.dealnote.messenger.mvp.view.IDrawerEditView;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.mvp.core.AbsPresenter;

/**
 * Created by Ruslan Kolbasa on 20.07.2017.
 * phoenix
 */
public class DrawerEditPresenter extends AbsPresenter<IDrawerEditView> {

    private final List<DrawerCategory> data;

    public DrawerEditPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.data = createInitialData();
    }

    @StringRes
    private static int getTitleResCategory(@SwitchableCategory int type) {
        switch (type) {
            case SwitchableCategory.FRIENDS:
                return R.string.friends;
            case SwitchableCategory.DIALOGS:
                return R.string.dialogs;
            case SwitchableCategory.FEED:
                return R.string.feed;
            case SwitchableCategory.FEEDBACK:
                return R.string.drawer_feedback;
            case SwitchableCategory.NEWSFEED_COMMENTS:
                return R.string.drawer_newsfeed_comments;
            case SwitchableCategory.GROUPS:
                return R.string.groups;
            case SwitchableCategory.PHOTOS:
                return R.string.photos;
            case SwitchableCategory.VIDEOS:
                return R.string.videos;
            case SwitchableCategory.MUSIC:
                return R.string.music;
            case SwitchableCategory.DOCS:
                return R.string.documents;
            case SwitchableCategory.BOOKMARKS:
                return R.string.bookmarks;
            case SwitchableCategory.SEARCH:
                return R.string.search;
        }

        throw new IllegalArgumentException();
    }

    private ArrayList<DrawerCategory> createInitialData() {
        ArrayList<DrawerCategory> categories = new ArrayList<>();

        ISettings.IDrawerSettings settings = Settings.get().drawerSettings();

        @SwitchableCategory
        int[] items = settings.getCategoriesOrder();

        for(int category : items){
            DrawerCategory c = new DrawerCategory(category, getTitleResCategory(category));
            c.setChecked(settings.isCategoryEnabled(category));
            categories.add(c);
        }

        return categories;
    }

    @Override
    public void onGuiCreated(@NonNull IDrawerEditView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    @Override
    protected String tag() {
        return DrawerEditPresenter.class.getSimpleName();
    }

    private void save() {
        @SwitchableCategory
        int[] postions = new int[data.size()];
        boolean[] active = new boolean[data.size()];

        for (int i = 0; i < data.size(); i++) {
            DrawerCategory category = data.get(i);

            postions[i] = category.getKey();
            active[i] = category.isChecked();
        }

        Settings.get().drawerSettings().setCategoriesOrder(postions, active);
    }

    public void fireSaveClick() {
        save();
        getView().goBackAndApplyChanges();
    }

    public void fireItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
    }
}
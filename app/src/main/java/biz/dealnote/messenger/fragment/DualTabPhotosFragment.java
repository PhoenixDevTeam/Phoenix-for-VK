package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.MyFragmentStatePagerAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.model.selection.AbsSelectableSource;
import biz.dealnote.messenger.model.selection.FileManagerSelectableSource;
import biz.dealnote.messenger.model.selection.LocalPhotosSelectableSource;
import biz.dealnote.messenger.model.selection.Sources;
import biz.dealnote.messenger.model.selection.Types;
import biz.dealnote.messenger.model.selection.VkPhotosSelectableSource;
import biz.dealnote.messenger.settings.CurrentTheme;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 15.04.2017.
 * phoenix
 */
public class DualTabPhotosFragment extends Fragment implements BackPressCallback {

    public static DualTabPhotosFragment newInstance(Sources sources) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.SOURCES, sources);

        DualTabPhotosFragment fragment = new DualTabPhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Sources mSources;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mSources = getArguments().getParcelable(Extra.SOURCES);

        if(nonNull(savedInstanceState)){
            this.mCurrentTab = savedInstanceState.getInt("mCurrentTab");
        }
    }

    private Adapter mPagerAdapter;
    private int mCurrentTab;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mCurrentTab", mCurrentTab);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_dual_tab_photos, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        TabLayout tabLayout = root.findViewById(R.id.tablayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        int tabColorPrimary = CurrentTheme.getPrimaryTextColorOnColoredBackgroundCode(getActivity());
        int tabColorSecondary = CurrentTheme.getSecondaryTextColorOnColoredBackgroundCode(getActivity());
        tabLayout.setTabTextColors(tabColorSecondary, tabColorPrimary);

        ViewPager viewPager = root.findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mCurrentTab = position;
            }
        });

        mPagerAdapter = new Adapter(getChildFragmentManager(), mSources);
        viewPager.setAdapter(mPagerAdapter);

        tabLayout.setupWithViewPager(viewPager, true);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.photos);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public boolean onBackPressed() {
        if(nonNull(mPagerAdapter)){
            Fragment fragment = mPagerAdapter.findFragmentByPosition(mCurrentTab);

            if(fragment instanceof BackPressCallback && !((BackPressCallback) fragment).onBackPressed()){
                return false;
            }
        }

        return true;
    }

    private class Adapter extends MyFragmentStatePagerAdapter {

        private final Sources mSources;

        public Adapter(FragmentManager fm, Sources mSources) {
            super(fm);
            this.mSources = mSources;
        }

        @Override
        public Fragment getItem(int position) {
            AbsSelectableSource source = mSources.get(position);

            if (source instanceof LocalPhotosSelectableSource) {
                Bundle args = new Bundle();
                args.putBoolean(BasePresenterFragment.EXTRA_HIDE_TOOLBAR, true);
                LocalImageAlbumsFragment fragment = new LocalImageAlbumsFragment();
                fragment.setArguments(args);
                return fragment;
            }

            if (source instanceof VkPhotosSelectableSource) {
                final VkPhotosSelectableSource vksource = (VkPhotosSelectableSource) source;
                VKPhotoAlbumsFragment fragment = VKPhotoAlbumsFragment.newInstance(vksource.getAccountId(), vksource.getOwnerId(), null, null);
                fragment.getArguments().putBoolean(BasePresenterFragment.EXTRA_HIDE_TOOLBAR, true);
                return fragment;
            }

            if(source instanceof FileManagerSelectableSource){
                Bundle args = new Bundle();
                args.putInt(Extra.ACTION, FileManagerFragment.SELECT_FILE);
                args.putBoolean(FileManagerFragment.EXTRA_SHOW_CANNOT_READ, true);

                FileManagerFragment fileManagerFragment = new FileManagerFragment();
                fileManagerFragment.setArguments(args);
                return fileManagerFragment;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            @Types
            int tabtype = mSources.get(position).getType();

            switch (tabtype) {
                case Types.LOCAL_PHOTOS:
                    return getString(R.string.local_photos_tab_title);

                case Types.VK_PHOTOS:
                    return getString(R.string.vk_photos_tab_title);

                case Types.FILES:
                    return getString(R.string.files_tab_title);
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            return mSources.count();
        }
    }
}
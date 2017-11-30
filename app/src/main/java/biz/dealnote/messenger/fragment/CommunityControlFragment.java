package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.settings.CurrentTheme;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityControlFragment extends Fragment {

    public static CommunityControlFragment newInstance(int accountId, Community community, GroupSettings settings) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.SETTINGS, settings);
        args.putParcelable(Extra.OWNER, community);
        CommunityControlFragment fragment = new CommunityControlFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Community mCommunity;
    //private GroupSettings mSettings;
    private int mAccountId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.mSettings = getArguments().getParcelable(Extra.SETTINGS);
        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.mCommunity = getArguments().getParcelable(Extra.OWNER);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_control, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        ViewPager pager = root.findViewById(R.id.view_pager);
        pager.setOffscreenPageLimit(2);

        List<ITab> tabs = new ArrayList<>();
        tabs.add(new Tab(getString(R.string.community_blacklist_tab_title), () -> CommunityBlacklistFragment.newInstance(mAccountId, mCommunity.getId())));
        tabs.add(new Tab(getString(R.string.community_links_tab_title), () -> CommunityLinksFragment.newInstance(mAccountId, mCommunity.getId())));

        if(mCommunity.getAdminLevel() >= VKApiCommunity.AdminLevel.ADMIN){
            tabs.add(new Tab(getString(R.string.community_managers_tab_title), () -> CommunityManagersFragment.newInstance(mAccountId, mCommunity.getId())));
        }

        pager.setAdapter(new Adapter(tabs, getChildFragmentManager()));

        TabLayout tabLayout = root.findViewById(R.id.tablayout);

        int tabColorPrimary = CurrentTheme.getPrimaryTextColorOnColoredBackgroundCode(getActivity());
        int tabColorSecondary = CurrentTheme.getSecondaryTextColorOnColoredBackgroundCode(getActivity());

        tabLayout.setTabTextColors(tabColorSecondary, tabColorPrimary);
        tabLayout.setupWithViewPager(pager);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.community_control);
        ActivityUtils.setToolbarSubtitle(this, mCommunity.getFullName());

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(true)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    private static class Tab implements ITab {

        final String title;
        final IFragmentCreator creator;

        private Tab(String title, IFragmentCreator creator) {
            this.title = title;
            this.creator = creator;
        }

        @Override
        public String getTabTitle() {
            return title;
        }

        @Override
        public IFragmentCreator getFragmentCreator() {
            return creator;
        }
    }

    private interface ITab {
        String getTabTitle();
        IFragmentCreator getFragmentCreator();
    }

    private interface IFragmentCreator {
        Fragment create();
    }

    private static class Adapter extends FragmentStatePagerAdapter {

        private final List<ITab> tabs;

        Adapter(List<ITab> tabs, FragmentManager fm) {
            super(fm);
            this.tabs = tabs;
        }

        @Override
        public Fragment getItem(int position) {
            return tabs.get(position).getFragmentCreator().create();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            /*switch (position){
                //case Tabs.OPTIONS:
                //    return app.getString(R.string.community_settings_tab_title);
                case Tabs.MANAGERS:
                    return app.getString(R.string.community_managers_tab_title);
                case Tabs.LINKS:
                    return app.getString(R.string.community_links_tab_title);
                case Tabs.BLACKLIST:
                    return app.getString(R.string.community_blacklist_tab_title);
                //case Tabs.MEMBERS:
                //    return app.getString(R.string.community_members_tab_title);
            }*/

            return tabs.get(position).getTabTitle();
        }

        @Override
        public int getCount() {
            return tabs.size();
        }
    }
}
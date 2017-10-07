package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.mvp.presenter.LikesListPresenter;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class LikesFragment extends AbsOwnersListFragment<LikesListPresenter, ISimpleOwnersView> {

    public static Bundle buildArgs(int accountId, String type, int ownerId, int itemId, String filter) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.TYPE, type);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ITEM_ID, itemId);
        args.putString(Extra.FILTER, filter);
        return args;
    }

    public static LikesFragment newInstance(@NonNull Bundle args) {
        LikesFragment fragment = new LikesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.mHasToolbar = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(actionBar != null){
            actionBar.setTitle("likes".equals(getArguments().getString(Extra.FILTER)) ? R.string.like : R.string.shared);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    protected String tag() {
        return LikesFragment.class.getSimpleName();
    }

    @Override
    public IPresenterFactory<LikesListPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LikesListPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getString(Extra.TYPE),
                getArguments().getInt(Extra.OWNER_ID),
                getArguments().getInt(Extra.ITEM_ID),
                getArguments().getString(Extra.FILTER),
                saveInstanceState
        );
    }
}

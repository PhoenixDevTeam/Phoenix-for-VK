package biz.dealnote.messenger.fragment.attachments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.RepostPresenter;
import biz.dealnote.messenger.mvp.view.IRepostView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.mvp.core.IPresenterFactory;
import biz.dealnote.mvp.reflect.OnGuiCreated;

public class RepostFragment extends AbsAttachmentsEditFragment<RepostPresenter, IRepostView> implements IRepostView {

    private static final String EXTRA_POST = "post";
    private static final String EXTRA_GROUP_ID = "group_id";

    public static RepostFragment obtain(Place place) {
        RepostFragment fragment = new RepostFragment();
        fragment.setArguments(place.getArgs());
        if (place.hasTargeting()) {
            fragment.setTargetFragment(place.target, place.requestCode);
        }

        return fragment;
    }

    public static RepostFragment newInstance(int accountId, Integer gid, Post post) {
        RepostFragment fragment = new RepostFragment();
        fragment.setArguments(buildArgs(accountId, gid, post));
        return fragment;
    }

    public static Bundle buildArgs(int accountId, Integer groupId, Post post) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_POST, post);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        if (groupId != null) {
            bundle.putInt(EXTRA_GROUP_ID, groupId);
        }

        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @OnGuiCreated
    public void goBack() {
        getActivity().onBackPressed();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ready:
                getPresenter().fireReadyClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.share);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    protected String tag() {
        return RepostFragment.class.getSimpleName();
    }

    @Override
    public IPresenterFactory<RepostPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            Post post = getArguments().getParcelable(EXTRA_POST);
            Integer groupId = getArguments().containsKey(EXTRA_GROUP_ID) ? getArguments().getInt(EXTRA_GROUP_ID) : null;
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            return new RepostPresenter(accountId, post, groupId, saveInstanceState);
        };
    }
}
package biz.dealnote.messenger.fragment.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.AbsOwnersListFragment;
import biz.dealnote.messenger.mvp.presenter.MutualFriendsPresenter;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class MutualFriendsFragment extends AbsOwnersListFragment<MutualFriendsPresenter, ISimpleOwnersView> implements ISimpleOwnersView {

    private static final String EXTRA_TARGET_ID = "targetId";

    public static MutualFriendsFragment newInstance(int accountId, int targetId){
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TARGET_ID, targetId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        MutualFriendsFragment friendsFragment = new MutualFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @Override
    protected String tag() {
        return MutualFriendsFragment.class.getSimpleName();
    }

    @Override
    public IPresenterFactory<MutualFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new MutualFriendsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(EXTRA_TARGET_ID),
                saveInstanceState
        );
    }
}
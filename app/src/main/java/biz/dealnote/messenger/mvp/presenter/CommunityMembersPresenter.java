package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityMembersView;

/**
 * Created by admin on 13.06.2017.
 * phoenix
 */
public class CommunityMembersPresenter extends AccountDependencyPresenter<ICommunityMembersView> {

    private static final String TAG = CommunityMembersPresenter.class.getSimpleName();

    private final int groupId;

    public CommunityMembersPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 08.09.2017.
 * phoenix
 */
public interface ISimpleOwnersView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayOwnerList(List<Owner> owners);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);

    void displayRefreshing(boolean refreshing);
    void showOwnerWall(int accoutnId, Owner owner);
}
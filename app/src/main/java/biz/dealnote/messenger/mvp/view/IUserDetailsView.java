package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.menu.AdvancedItem;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public interface IUserDetailsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayData(@NonNull List<AdvancedItem> items);

    void displayToolbarTitle(String title);

    void openOwnerProfile(int accountId, int ownerId, @Nullable Owner owner);
}
package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 21.06.2017.
 * phoenix
 */
public interface ICommunityManagerEditView extends IMvpView, IAccountDependencyView, IErrorView, IProgressView, IToastView {
    void displayUserInfo(User user);

    void showUserProfile(int accountId, User user);

    void checkModerator();
    void checkEditor();
    void checkAdmin();

    void setShowAsContactCheched(boolean cheched);
    void setContactInfoVisible(boolean visible);

    void displayPosition(String position);
    void displayEmail(String email);
    void displayPhone(String phone);

    void configRadioButtons(boolean isCreator);

    void goBack();

    void setDeleteOptionVisible(boolean visible);
}
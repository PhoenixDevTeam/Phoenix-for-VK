package biz.dealnote.messenger.mvp.view.search;

import biz.dealnote.messenger.model.User;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public interface IPeopleSearchView extends IBaseSearchView<User> {
    void openUserWall(int accountId, User user);
}
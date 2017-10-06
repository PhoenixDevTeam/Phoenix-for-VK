package biz.dealnote.messenger.activity;

import biz.dealnote.messenger.model.SelectProfileCriteria;
import biz.dealnote.messenger.model.User;

public interface ProfileSelectable {

    void select(User user);

    SelectProfileCriteria getAcceptableCriteria();
}
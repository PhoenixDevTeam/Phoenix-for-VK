package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 15.06.2017.
 * phoenix
 */
public interface IGroupSettingsInteractor {

    Single<GroupSettings> getGroupSettings(int accountId, int groupId);

    Completable banUser(int accountId, int groupId, int userId, Long endDateUnixtime, int reason, String comment, boolean showCommentToUser);

    Completable editManager(int accountId, int groupId, User user, String role, boolean asContact, String position, String email, String phone);

    Completable unbanUser(int accountId, int groupId, int userId);

    Single<Pair<List<Banned>, IntNextFrom>> getBanned(int accountId, int groupId, IntNextFrom startFrom, int count);

    Single<List<Manager>> getManagers(int accountId, int groupId);
}
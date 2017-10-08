package biz.dealnote.messenger.domain;

import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public interface IBlacklistRepository {
    Completable fireAdd(int accountId, User user);
    Completable fireRemove(int accountId, int userId);

    Observable<Pair<Integer, User>> observeAdding();
    Observable<Pair<Integer, Integer>> observeRemoving();
}
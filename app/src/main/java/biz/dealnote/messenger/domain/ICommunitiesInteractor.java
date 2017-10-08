package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.Community;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public interface ICommunitiesInteractor {
    Single<List<Community>> getCachedData(int accountId, int userId);
    Single<List<Community>> getActual(int accountId, int userId, int count, int offset);
    Single<List<Community>> search(int accountId, String q, String type, Integer countryId, Integer cityId, Boolean futureOnly,
                                   Integer sort, int count, int offset);

    Completable join(int accountId, int groupId);
    Completable leave(int accountId, int groupId);
}
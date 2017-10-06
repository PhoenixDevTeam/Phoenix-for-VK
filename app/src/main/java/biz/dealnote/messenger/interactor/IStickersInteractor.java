package biz.dealnote.messenger.interactor;

import io.reactivex.Completable;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public interface IStickersInteractor {
    Completable getAndStore(int accountId);
}

package biz.dealnote.messenger.domain.impl;

import biz.dealnote.messenger.domain.IBlacklistRepository;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by admin on 09.07.2017.
 * phoenix
 */
public class BlacklistRepository implements IBlacklistRepository {

    private final PublishSubject<Pair<Integer, User>> addPublisher;
    private final PublishSubject<Pair<Integer, Integer>> removePublisher;

    public BlacklistRepository() {
        this.addPublisher = PublishSubject.create();
        this.removePublisher = PublishSubject.create();
    }

    @Override
    public Completable fireAdd(int accountId, User user) {
        return Completable.fromAction(() -> addPublisher.onNext(Pair.create(accountId, user)));
    }

    @Override
    public Completable fireRemove(int accountId, int userId) {
        return Completable.fromAction(() -> removePublisher.onNext(Pair.create(accountId, userId)));
    }

    @Override
    public Observable<Pair<Integer, User>> observeAdding() {
        return addPublisher;
    }

    @Override
    public Observable<Pair<Integer, Integer>> observeRemoving() {
        return removePublisher;
    }
}
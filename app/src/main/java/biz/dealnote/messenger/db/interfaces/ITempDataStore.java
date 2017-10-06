package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.db.serialize.ISerializeAdapter;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public interface ITempDataStore {
    <T> Single<List<T>> getData(int ownerId, int sourceId, ISerializeAdapter<T> serializer);
    <T> Completable put(int ownerId, int sourceId, List<T> data, ISerializeAdapter<T> serializer);

    Completable delete(int ownerId);
}
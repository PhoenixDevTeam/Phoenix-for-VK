package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.StickerSet;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 20.03.2017.
 * phoenix
 */
public interface IStickersInteractor {
    Completable getAndStore(int accountId);
    Single<List<StickerSet>> getStickers(int accountId);
}
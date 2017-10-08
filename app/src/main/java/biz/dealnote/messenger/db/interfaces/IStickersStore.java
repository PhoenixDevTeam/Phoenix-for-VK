package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.model.StickerSet;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStickersStore extends IStore {

    Completable store(int accountId, List<VKApiStickerSet.Product> sets);

    Single<List<StickerSet>> getPurchasedAndActive(int accountId);
}
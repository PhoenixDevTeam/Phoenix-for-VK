package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.StickerSetEntity;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public interface IStickersStorage extends IStorage {

    Completable store(int accountId, List<StickerSetEntity> sets);

    Single<List<StickerSetEntity>> getPurchasedAndActive(int accountId);
}
package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.DocumentEntity;
import biz.dealnote.messenger.model.criteria.DocsCriteria;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public interface IDocsStore extends IStore {

    @CheckResult
    Single<List<DocumentEntity>> get(@NonNull DocsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<DocumentEntity> entities, boolean clearBeforeInsert);

    @CheckResult
    Completable delete(int accountId, int docId, int ownerId);
}
package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.criteria.DocsCriteria;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public interface IDocsRepository extends IRepository {

    @CheckResult
    Single<List<Document>> get(@NonNull DocsCriteria criteria);

    @CheckResult
    Maybe<DatabaseIdRange> store(int accountId, int ownerId, List<VkApiDoc> dtos, boolean clearBeforeInsert);
}

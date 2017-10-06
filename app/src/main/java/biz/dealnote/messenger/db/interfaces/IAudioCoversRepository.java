package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 * Created by admin on 22.11.2016.
 * phoenix
 */
public interface IAudioCoversRepository extends IRepository {

    @CheckResult
    Completable saveCover(int accountId, int audioId, int ownerId, CoverSearchResult coverSearchResult);

    @CheckResult
    Maybe<CoverSearchResult> findCoverFor(int accounId, int audioId, int ownerId);
}
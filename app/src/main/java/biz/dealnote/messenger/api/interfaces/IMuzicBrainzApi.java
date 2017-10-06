package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import biz.dealnote.messenger.api.model.musicbrainz.RecordingSearchResult;
import io.reactivex.Single;

/**
 * Created by admin on 20.01.2017.
 * phoenix
 */
public interface IMuzicBrainzApi {

    @CheckResult
    Single<RecordingSearchResult> recording(String query);

    @CheckResult
    Single<CoverSearchResult> releaseCovers(String mdib);
}

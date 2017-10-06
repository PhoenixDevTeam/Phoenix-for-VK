package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import biz.dealnote.messenger.api.model.musicbrainz.RecordingSearchResult;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by admin on 20.01.2017.
 * phoenix
 */
public interface IMusicBrainzService {

    @GET("ws/2/recording")
    Single<RecordingSearchResult> recording(@Query("query") String query, @Query("fmt") String fmt);

    @GET("release/{mdib}")
    Single<CoverSearchResult> releaseCovers(@Path("mdib") String mdib);
}

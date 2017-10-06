package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.RetrofitFactory;
import biz.dealnote.messenger.api.interfaces.IMuzicBrainzApi;
import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import biz.dealnote.messenger.api.model.musicbrainz.RecordingSearchResult;
import biz.dealnote.messenger.api.services.IMusicBrainzService;
import io.reactivex.Single;

/**
 * Created by admin on 20.01.2017.
 * phoenix
 */
class MusicBrainzApi implements IMuzicBrainzApi {

    @Override
    public Single<RecordingSearchResult> recording(String query) {
        return service().recording(query, "json");
    }

    @Override
    public Single<CoverSearchResult> releaseCovers(String mdib) {
        return RetrofitFactory.createCoverartArchiveRetrofit()
                .create(IMusicBrainzService.class)
                .releaseCovers(mdib);
    }

    private IMusicBrainzService service(){
        return RetrofitFactory.createMuzicBrainzRetrofit().create(IMusicBrainzService.class);
    }
}
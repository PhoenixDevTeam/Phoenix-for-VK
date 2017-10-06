package biz.dealnote.messenger.service.operations.audio;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import biz.dealnote.messenger.api.model.musicbrainz.Recording;
import biz.dealnote.messenger.api.model.musicbrainz.RecordingSearchResult;
import biz.dealnote.messenger.api.model.musicbrainz.Release;
import biz.dealnote.messenger.db.Repositories;
import biz.dealnote.messenger.model.Cover;
import biz.dealnote.messenger.service.operations.AbsApiOperation;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class AlbumCoverFindOperation extends AbsApiOperation {

    private static final String TAG = AlbumCoverFindOperation.class.getSimpleName();

    private static final int MAX_TRIES = 5;

    private String getSmallImage(CoverSearchResult result) {
        if (nonNull(result)
                && !safeIsEmpty(result.images)
                && nonNull(result.images.get(0).thumbnails)) {
            return result.images.get(0).thumbnails.small;
        }

        return null;
    }

    private String getLargeImage(CoverSearchResult result) {
        if (nonNull(result)
                && !safeIsEmpty(result.images)
                && nonNull(result.images.get(0).thumbnails)) {
            return result.images.get(0).thumbnails.large;
        }

        return null;
    }

    private CoverSearchResult getCoverFromNet(String artist, String title){
        String query = "artist:(" + artist + ")+recording:(" + title + ")";

        RecordingSearchResult recordingSearchResult = Apis.get()
                .musicBrains()
                .recording(query)
                .retry(MAX_TRIES)
                .blockingGet();

        if (nonNull(recordingSearchResult.recordings)) {
            for (Recording recording : recordingSearchResult.recordings) {
                if (recording.score < 50) {
                    Logger.d(TAG, "No cover art");
                    break;
                }

                CoverSearchResult coverSearchResult = processRecording(recording);

                if(nonNull(coverSearchResult)){
                   return coverSearchResult;
                }
            }
        }

        return null;
    }

    private CoverSearchResult processRecording(Recording recording) {
        Logger.d(TAG, "recording: " + recording);

        if (safeIsEmpty(recording.releases)) {
            return null;
        }

        for (Release release : recording.releases) {
            CoverSearchResult coverSearchResult = null;

            try {
                coverSearchResult = Apis.get()
                        .musicBrains()
                        .releaseCovers(release.id)
                        .blockingGet();
            } catch (Exception ignored){
               ignored.printStackTrace();
            }

            Logger.d(TAG, "coverSearchResult: " + coverSearchResult);

            if (coverSearchResult != null) {
                return coverSearchResult;
            }

            //try {
            //    Thread.sleep(1000);
            //} catch (InterruptedException ignored) {
            //}
        }

        return null;
    }

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int audioId = request.getInt(Extra.ID);
        int ownerId = request.getInt(Extra.OWNER_ID);
        String artist = request.getString(Extra.ARTIST);
        String title = request.getString(Extra.TITLE);

        Logger.d(TAG, "audioId: " + audioId + ", ownerId: " + ownerId + ", artist: " + artist + ", title: " + title);

        CoverSearchResult coverSearchResult = Repositories.getInstance()
                .audioCovers()
                .findCoverFor(accountId, audioId, ownerId)
                .blockingGet();

        if (coverSearchResult == null) {
            coverSearchResult = getCoverFromNet(artist, title);

            if (coverSearchResult != null) {
                Repositories.getInstance()
                        .audioCovers()
                        .saveCover(accountId, audioId, ownerId, coverSearchResult)
                        .blockingAwait();
            }
        }

        String large = getLargeImage(coverSearchResult);
        String small = getSmallImage(coverSearchResult);

        Bundle bundle = new Bundle();

        if (!safeIsEmpty(large) || !safeIsEmpty(small)) {
            Cover cover = new Cover();
            cover.audioId = audioId;
            cover.ownerId = ownerId;
            cover.large = large;
            cover.small = small;

            bundle.putParcelable(Extra.COVER, cover);
        }

        return bundle;
    }
}

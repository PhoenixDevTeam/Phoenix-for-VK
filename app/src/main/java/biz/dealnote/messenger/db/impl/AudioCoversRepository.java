package biz.dealnote.messenger.db.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.model.musicbrainz.CoverSearchResult;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.column.CoversColumns;
import biz.dealnote.messenger.db.interfaces.IAudioCoversRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 22.11.2016.
 * phoenix
 */
class AudioCoversRepository extends AbsRepository implements IAudioCoversRepository {

    AudioCoversRepository(@NonNull AppRepositories base) {
        super(base);
    }

    @Override
    public Completable saveCover(int accountId, int audioId, int ownerId, CoverSearchResult coverSearchResult) {
        return Completable.create(e -> {
            ContentValues cv = new ContentValues();
            cv.put(CoversColumns.AUDIO_ID, audioId);
            cv.put(CoversColumns.OWNER_ID, ownerId);
            cv.put(CoversColumns.DATA, GSON.toJson(coverSearchResult));

            Uri uri = MessengerContentProvider.getCoversContentUriFor(accountId);
            getContentResolver().insert(uri, cv);
            e.onComplete();
        });
    }

    @Override
    public Maybe<CoverSearchResult> findCoverFor(int accounId, int audioId, int ownerId) {
        return Maybe.create(e -> {
            String where = CoversColumns.AUDIO_ID + " = ? AND " + CoversColumns.OWNER_ID + " = ?";
            String[] args = {String.valueOf(audioId), String.valueOf(ownerId)};
            Uri uri = MessengerContentProvider.getCoversContentUriFor(accounId);

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            CoverSearchResult result = null;

            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    String blob = cursor.getString(cursor.getColumnIndex(CoversColumns.DATA));
                    result = GSON.fromJson(blob, CoverSearchResult.class);
                }

                cursor.close();
            }

            if(nonNull(result)){
                e.onSuccess(result);
            }

            e.onComplete();
        });
    }
}

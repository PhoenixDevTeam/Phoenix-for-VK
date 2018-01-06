package biz.dealnote.messenger.domain.impl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class AudioInteractor implements IAudioInteractor {

    private final INetworker networker;
    private final Context app;

    public AudioInteractor(Context context, INetworker networker) {
        this.networker = networker;
        this.app = context.getApplicationContext();
    }

    @Override
    public Single<Audio> add(int accountId, Audio orig, Integer groupId, Integer albumId) {
        return networker.vkDefault(accountId)
                .audio()
                .add(orig.getId(), orig.getOwnerId(), groupId, albumId)
                .map(resultId -> {
                    final int targetOwnerId = Objects.nonNull(groupId) ? -groupId : accountId;
                    //clone
                    return new Audio()
                            .setId(resultId)
                            .setOwnerId(targetOwnerId)
                            .setAlbumId(Objects.nonNull(albumId) ? albumId : 0)
                            .setArtist(orig.getArtist())
                            .setTitle(orig.getTitle())
                            .setUrl(orig.getUrl())
                            .setLyricsId(orig.getLyricsId())
                            .setGenre(orig.getGenre())
                            .setDuration(orig.getDuration());
                });
    }

    @Override
    public Completable delete(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .delete(audioId, ownerId)
                .toCompletable();
    }

    @Override
    public Completable restore(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .restore(audioId, ownerId)
                .toCompletable();
    }

    @Override
    public Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, Collection<Integer> targetIds) {
        return networker.vkDefault(accountId)
                .audio()
                .setBroadcast(new IdPair(audioId, audioOwnerId), targetIds)
                .toCompletable();
    }

    private static void checkAudioPluginError(Cursor cursor) throws AudioPluginException {
        if(cursor.getPosition() == 0){
            int errorCodeIndex = cursor.getColumnIndex("error_code");

            if(errorCodeIndex >= 0){
                int code = cursor.getInt(errorCodeIndex);
                String message = cursor.getString(cursor.getColumnIndex("error_message"));
                throw new AudioPluginException(code, message);
            }
        }
    }

    @Override
    public Single<List<Audio>> get(int ownerId, int offset) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority("biz.dealnote.phoenix.AudioProvider")
                    .path("audios")
                    .appendQueryParameter("owner_id", String.valueOf(ownerId))
                    .appendQueryParameter("offset", String.valueOf(offset))
                    .build();

            Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);

            List<Audio> audios = new ArrayList<>(Utils.safeCountOf(cursor));

            if(cursor != null){
                while (cursor.moveToNext()){
                    if(emitter.isDisposed()){
                        break;
                    }

                    checkAudioPluginError(cursor);

                    int audioId = cursor.getInt(cursor.getColumnIndex("audio_id"));
                    int ownerId1 = cursor.getInt(cursor.getColumnIndex("owner_id"));
                    String title = cursor.getString(cursor.getColumnIndex("title"));
                    String artist = cursor.getString(cursor.getColumnIndex("artist"));
                    int duration = cursor.getInt(cursor.getColumnIndex("duration"));

                    Audio audio = new Audio()
                            .setArtist(artist)
                            .setDuration(duration)
                            .setId(audioId)
                            .setOwnerId(ownerId1)
                            .setTitle(title);

                    audios.add(audio);
                }

                cursor.close();
            }

            emitter.onSuccess(audios);
        });
    }

    @Override
    public Single<String> findAudioUrl(int audioId, int ownerId) {
        return Single.fromCallable(() -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority("biz.dealnote.phoenix.AudioProvider")
                    .path("urls")
                    .appendQueryParameter("owner_id", String.valueOf(ownerId))
                    .appendQueryParameter("audio_id", String.valueOf(audioId))
                    .build();

            Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);

            String url = null;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    url = cursor.getString(cursor.getColumnIndex("url"));
                    Logger.d("Audios", "audioId: " + audioId);
                }

                cursor.close();
            }

            if(url == null){
                throw new NotFoundException();
            }

            return url;
        });
    }

    public static final class AudioPluginException extends Exception {

        final int code;

        AudioPluginException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
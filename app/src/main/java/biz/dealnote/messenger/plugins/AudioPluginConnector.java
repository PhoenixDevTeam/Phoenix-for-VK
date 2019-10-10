package biz.dealnote.messenger.plugins;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by admin on 2/3/2018.
 * Phoenix-for-VK
 */
public class AudioPluginConnector implements IAudioPluginConnector {

    private static final String AUTHORITY = "biz.dealnote.phoenix.AudioProvider";

    private final Context app;

    public AudioPluginConnector(Context context) {
        this.app = context.getApplicationContext();
    }

    @Override
    public Single<List<Audio>> get(int accountId, int ownerId, int offset) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("audios")
                    .appendQueryParameter("request", "get")
                    .appendQueryParameter("account_id", String.valueOf(accountId))
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

                    try {
                        checkAudioPluginError(cursor);
                    } catch (Exception e) {
                        cursor.close();
                        emitter.onError(e);
                        return;
                    }

                    audios.add(map(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(audios);
        });
    }

    private static Audio map(Cursor cursor){
        int audioId = cursor.getInt(cursor.getColumnIndex("audio_id"));
        int ownerId = cursor.getInt(cursor.getColumnIndex("owner_id"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String artist = cursor.getString(cursor.getColumnIndex("artist"));
        String url = cursor.getString(cursor.getColumnIndex("url"));
        int duration = cursor.getInt(cursor.getColumnIndex("duration"));
        String cover = cursor.getString(cursor.getColumnIndex("cover_url"));
        String bigCover = cursor.getString(cursor.getColumnIndex("cover_url_big"));

        return new Audio()
                .setUrl(url)
                .setArtist(artist)
                .setDuration(duration)
                .setId(audioId)
                .setOwnerId(ownerId)
                .setTitle(title)
                .setBigCover(bigCover)
                .setCover(cover);
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
    public Single<String> findAudioUrl(int accountId, int audioId, int ownerId) {
        return Single.create(emitter -> {
            String audios = ownerId + "_" + audioId;

            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("audios")
                    .appendQueryParameter("request", "getById")
                    .appendQueryParameter("account_id", String.valueOf(accountId))
                    .appendQueryParameter("audios", audios)
                    .build();

            Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);

            String url = null;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    url = map(cursor).getUrl();
                }

                cursor.close();
            }

            if (isNull(url)) {
                emitter.onError(new NotFoundException());
            } else {
                emitter.onSuccess(url);
            }
        });
    }

    @Override
    public boolean isPluginAvailable() {
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .path("availability")
                .build();

        boolean available = false;

        try {
            Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);
            if(cursor != null){
                if(cursor.moveToNext()){
                    available = cursor.getInt(cursor.getColumnIndex("available")) == 1;
                }
                cursor.close();
            }
        } catch (Exception ignored){

        }

        return available;
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
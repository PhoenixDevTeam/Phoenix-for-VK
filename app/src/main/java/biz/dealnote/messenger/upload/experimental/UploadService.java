package biz.dealnote.messenger.upload.experimental;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.util.IOUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Call;

/**
 * Created by ruslan.kolbasa on 06.10.2016.
 * phoenix
 */
public class UploadService {

    public static Observable<UploadStatus<UploadDocDto>> uploadVoiceMessage(@NonNull UploadServer server, @NonNull File file){
        return Observable.create(e -> {
            PercentageListener listener = percentage -> e.onNext(new UploadStatus<>(percentage));

            InputStream is = new FileInputStream(file);

            try {
                Call<UploadDocDto> call = Apis.get()
                        .uploads()
                        .uploadDocument(server.getUrl(), file.getName(), is, listener);

                UploadDocDto dto = call.execute()
                        .body();

                e.onNext(new UploadStatus<>(dto));
                e.onComplete();
            } finally {
                IOUtils.closeStreamQuietly(is);
            }
        });
    }

    public static Single<UploadDocDto> uploadDocument(@NonNull INetworker networker, @NonNull UploadServer server, @NonNull File file){
        return Single.create(emitter -> {
            InputStream is = new FileInputStream(file);

            try {
                Call<UploadDocDto> call = networker
                        .uploads()
                        .uploadDocument(server.getUrl(), file.getName(), is, percentage -> {});

                emitter.setCancellable(call::cancel);

                UploadDocDto dto = call.execute().body();

                emitter.onSuccess(dto);
            } finally {
                IOUtils.closeStreamQuietly(is);
            }
        });
    }
}
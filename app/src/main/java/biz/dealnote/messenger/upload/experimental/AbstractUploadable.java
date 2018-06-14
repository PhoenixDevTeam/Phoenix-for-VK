package biz.dealnote.messenger.upload.experimental;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.upload.UploadObject;
import io.reactivex.Completable;
import io.reactivex.Single;

abstract class AbstractUploadable<T> {
    abstract Single<UploadServer> obtainServer(@NonNull UploadObject upload);

    abstract Single<T> doUpload(@NonNull UploadServer server, @NonNull UploadObject upload, @Nullable PercentageListener listener);

    abstract Completable commit(@NonNull UploadObject upload, @NonNull T result);

    final Single<UploadResult<T>> start(@Nullable UploadServer cachedServer, @NonNull UploadObject upload, @Nullable PercentageListener listener){
        final Single<UploadServer> serverSingle;

        if(cachedServer != null){
            serverSingle = Single.just(cachedServer);
        } else {
            serverSingle = obtainServer(upload);
        }

        return serverSingle.flatMap(server -> doUpload(server, upload, listener)
                .flatMap(t -> {
                    UploadResult<T> result = new UploadResult<>(server, t);

                    if(upload.isAutoCommit()){
                        return commit(upload, t).andThen(Single.just(result));
                    }

                    return Single.just(result);
                }));
    }
}
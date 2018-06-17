package biz.dealnote.messenger.upload;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.model.server.UploadServer;
import io.reactivex.Single;

public interface IUploadable<T> {
    Single<UploadResult<T>> doUpload(@NonNull Upload upload,
                                     @Nullable UploadServer initialServer,
                                     @Nullable PercentagePublisher listener);
}
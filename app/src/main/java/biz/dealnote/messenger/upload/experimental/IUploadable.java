package biz.dealnote.messenger.upload.experimental;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.upload.UploadObject;
import io.reactivex.Single;

public interface IUploadable<T> {
    Single<UploadResult<T>> doUpload(@NonNull UploadObject upload,
                                     @Nullable UploadServer initialServer,
                                     @Nullable PercentagePublisher listener);
}
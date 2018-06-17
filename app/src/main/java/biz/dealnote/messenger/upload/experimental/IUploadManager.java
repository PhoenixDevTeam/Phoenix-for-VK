package biz.dealnote.messenger.upload.experimental;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface IUploadManager {
    Single<List<UploadObject>> get(int accountId, @NonNull UploadDestination destination);

    void enqueue(@NonNull List<UploadIntent> intents);

    void cancel(int id);

    void cancelAll(int accountId, @NonNull UploadDestination destination);

    Optional<UploadObject> getCurrent();

    Flowable<int[]> observeDeleting(boolean includeCompleted);

    Flowable<List<UploadObject>> observeAdding();

    Flowable<UploadObject> obseveStatus();

    Flowable<Pair<UploadObject, UploadResult<?>>> observeResults();

    Flowable<List<IProgressUpdate>> observeProgress();

    interface IProgressUpdate {
        int getId();
        int getProgress();
    }
}
package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Predicate;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 27.01.2017.
 * phoenix
 */
public interface IUploadQueueStore {

    Single<List<UploadObject>> getAll(Predicate<UploadObject> filter);

    Single<List<UploadObject>> getByDestination(int accountId, int destId, int destOwnerId, @Method int method);

    Single<List<UploadObject>> getByDestination(int accountId, UploadDestination dest);

    @CheckResult
    Single<List<UploadObject>> put(List<UploadIntent> intents);

    Observable<List<IProgressUpdate>> observeProgress();

    Observable<IStatusUpdate> observeStatusUpdates();

    @CheckResult
    Completable removeWithId(int id);

    @CheckResult
    Completable removeWithId(int id, BaseUploadResponse response);

    Single<Optional<UploadObject>> findFirstByStatus(int status);

    @CheckResult
    Completable changeStatus(int id, int status);

    Observable<List<IQueueUpdate>> observeQueue();

    void changeProgress(int id, int progress);

    interface IProgressUpdate {
        int getId();

        int getProgress();
    }

    interface IStatusUpdate {
        int getId();

        int getStatus();
    }

    interface IQueueUpdate {

        int getId();

        @Nullable
        UploadObject object();

        /**
         * @return true if adding, false if removing
         */
        boolean isAdding();

        @Nullable
        BaseUploadResponse response();
    }
}

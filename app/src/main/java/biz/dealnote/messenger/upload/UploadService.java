package biz.dealnote.messenger.upload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.interfaces.IMessagesStore;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.upload.task.AbstractUploadTask;
import biz.dealnote.messenger.upload.task.DocumentUploadTask;
import biz.dealnote.messenger.upload.task.OwnerPhotoUploadTask;
import biz.dealnote.messenger.upload.task.PhotoMessageUploadTask;
import biz.dealnote.messenger.upload.task.PhotoToAlbumTask;
import biz.dealnote.messenger.upload.task.PhotoWallUploadTask;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.MagicKey;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

public class UploadService extends Service implements UploadCallback {

    private static final String TAG = UploadService.class.getSimpleName();

    public static final String ACTION_UPLOAD_FIRST = BuildConfig.APPLICATION_ID + ".service.ACTION_UPLOAD_FIRST";
    public static final String ACTION_CANCEL_BY_DESTINATION = BuildConfig.APPLICATION_ID + ".service.ACTION_CANCEL_BY_DESTINATION";
    public static final String ACTION_CANCEL_BY_ID = BuildConfig.APPLICATION_ID + ".service.ACTION_CANCEL_BY_ID";

    private final IBinder mBinder = new ServiceStub(this);

    private IUploadQueueStore uploadsRepository;
    private IMessagesStore messagesStore;

    private AbstractUploadTask<?> mCurrenTask;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        this.uploadsRepository = Injection.provideStores().uploads();
        this.messagesStore = Injection.provideStores().messages();
    }

    private void uploadFirstInQueue() {
        if (nonNull(mCurrenTask)) {
            Logger.d(TAG, "Current task is not null!!!");
            return;
        }

        compositeDisposable.add(uploadsRepository
                .findFirstByStatus(UploadObject.STATUS_QUEUE)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::uploadFirstInQueue, Analytics::logUnexpectedError));
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    private void uploadFirstInQueue(Optional<UploadObject> optionalFirst) {
        if(optionalFirst.isEmpty()){
            stopSelf();
            return;
        }

        UploadObject first = optionalFirst.get();

        Logger.d(TAG, "uploadFirstInQueue, first: " + first);

        if (mCurrenTask != null) {
            Logger.e(TAG, "Current task is not null. Fix, please !!!");
            return;
        }

        UploadDestination dest = first.getDestination();
        UploadServer server = findServerFor(first);

        switch (dest.getMethod()) {
            case Method.PHOTO_TO_MESSAGE:
                mCurrenTask = new PhotoMessageUploadTask(this, this, first, server);
                break;

            case Method.PHOTO_TO_COMMENT: // к комментариям изображения прикрепляются аналогично со стеной
            case Method.PHOTO_TO_WALL:
                mCurrenTask = new PhotoWallUploadTask(this, this, first, server);
                break;

            case Method.PHOTO_TO_PROFILE:
                mCurrenTask = new OwnerPhotoUploadTask(this, this, first, server);
                break;

            case Method.DOCUMENT:
                mCurrenTask = new DocumentUploadTask(this, this, first, server);
                break;

            case Method.PHOTO_TO_ALBUM:
                mCurrenTask = new PhotoToAlbumTask(this, this, first, server);
                break;
            default:
                throw new IllegalArgumentException("Unsupposted upload method, value: " + first.getDestination().getMethod());
        }

        //mCurrenTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mCurrenTask.startAsync();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();

        Logger.d(TAG, "onStartCommand, action: " + action);

        if (ACTION_CANCEL_BY_DESTINATION.equals(action)) {
            UploadDestination destination = intent.getParcelableExtra(Extra.DESTINATION);
            cancelByDestination(destination);
        }

        //if (mCurrenTask == null) {
        //    uploadFirstInQueue();
        //}

        if (ACTION_CANCEL_BY_ID.equalsIgnoreCase(action)) {
            AssertUtils.requireNonNull(intent);
            AssertUtils.requireNonNull(intent.getExtras());

            int id = intent.getExtras().getInt(Extra.ID);
            cancelById(id);
        }

        if (ACTION_UPLOAD_FIRST.equalsIgnoreCase(action)) {
            uploadFirstInQueue();
        }

        return START_NOT_STICKY;
    }

    private void cancelByDestination(@NonNull UploadDestination dest) {
        compositeDisposable.add(uploadsRepository
                .getAll(object -> object.getDestination().compareTo(dest))
                .flatMapObservable(Observable::fromIterable)
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(object -> cancelById(object.getId()), Analytics::logUnexpectedError));
    }

    @Override
    public void onPrepareToUpload(UploadObject current) {
        current.setStatus(UploadObject.STATUS_UPLOADING);
        current.setProgress(0);
        current.setErrorText(null);

        fireStatusChange(current);
    }

    @Override
    public void onProgressUpdate(UploadObject uploadObject, int primaryProgress) {
        uploadsRepository.changeProgress(uploadObject.getId(), primaryProgress);
    }

    @Override
    public void onError(UploadObject upload, Throwable throwable) {
        String localizedMessage = ErrorLocalizer.localizeThrowable(this, getCauseIfRuntime(throwable));

        upload.setErrorText(localizedMessage);
        upload.setProgress(0);
        upload.setStatus(UploadObject.STATUS_ERROR);

        fireStatusChange(upload);
        mCurrenTask = null;

        uploadFirstInQueue();

        Toast.makeText(this, localizedMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess(UploadObject uploadObject, BaseUploadResponse response) {
        compositeDisposable.add(handleCompletedUploading(uploadObject, response)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(needToStartSendService -> {
                    if (needToStartSendService) {
                        startSendService();
                    }

                    if (nonNull(response.getServer())) {
                        saveServerToCache(uploadObject, response.getServer());
                    }

                    mCurrenTask = null;
                    uploadFirstInQueue();
                }, Analytics::logUnexpectedError));
    }

    private void startSendService() {
        startService(new Intent(this, SendService.class));
    }

    private Single<Boolean> handleCompletedUploading(final UploadObject upload, BaseUploadResponse response) {
        final UploadDestination dest = upload.getDestination();
        final int accountId = upload.getAccountId();

        // удаляем задачу из очереди
        return uploadsRepository.removeWithId(upload.getId(), response)
                // если загружали в личное сообщение, то отправляем это сообщение (в случае, если оно с статусе "Ожидание загрузки")
                .andThen(dest.getMethod() != Method.PHOTO_TO_MESSAGE ? Single.just(false) : uploadsRepository.getByDestination(accountId, dest)
                        .flatMap(data -> {
                            if (data.isEmpty()) {
                                return analyzeMessageTrueIfNeedSend(accountId, dest.getId());
                            }

                            return Single.just(false);
                        }));
    }

    private Single<Boolean> analyzeMessageTrueIfNeedSend(int accountId, int dbid) {
        return messagesStore.getMessageStatus(accountId, dbid)
                .flatMap(status -> {
                    if (status == MessageStatus.WAITING_FOR_UPLOAD) {
                        return messagesStore
                                .changeMessageStatus(accountId, dbid, MessageStatus.QUEUE, null)
                                .andThen(Single.just(true));
                    }

                    return Single.just(false);
                })
                .onErrorReturnItem(false);
    }

    public UploadObject getCurrent() {
        return mCurrenTask == null ? null : mCurrenTask.getUploadObject();
    }

    @Override
    public void onCanceled(UploadObject upload) {
        mCurrenTask = null;

        uploadsRepository.removeWithId(upload.getId())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Analytics::logUnexpectedError);

        uploadFirstInQueue();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void fireStatusChange(UploadObject uploadObject) {
        uploadsRepository.changeStatus(uploadObject.getId(), uploadObject.getStatus())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, Analytics::logUnexpectedError);
    }

    /**
     * Отменить загрузку обьекта
     *
     * @param id идентификатор загружаемого обьекта
     * @return если загрузка отмнена незамедлительно - true,
     * если неоходимо ждать завершения asynctask - false
     */
    private boolean cancelById(int id) {
        Logger.d(TAG, "cancelById, id: " + id);

        UploadObject current = getCurrent();

        if (current != null && current.getId() == id) {
            current.setStatus(UploadObject.STATUS_CANCELLING);
            current.setProgress(0);
            current.setErrorText(null);

            fireStatusChange(current);

            mCurrenTask.cancelUploading();
            return false;
        }

        uploadsRepository.removeWithId(id)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, Analytics::logUnexpectedError);

        return true;
    }

    private HashMap<MagicKey, UploadServer> mServersMap = new HashMap<>();

    private void saveServerToCache(UploadObject upload, UploadServer server) {
        mServersMap.put(createKey(upload), server);
    }

    private UploadServer findServerFor(UploadObject upload) {
        MagicKey key = createKey(upload);
        UploadServer targetServer = mServersMap.get(key);

        Logger.d(TAG, "Try to re-use server, from cache: " + (targetServer == null ? "null" : targetServer.getUrl()) + ", key: " + key);
        return targetServer;
    }

    @NonNull
    private MagicKey createKey(UploadObject upload) {
        UploadDestination dest = upload.getDestination();

        MagicKey bundle = new MagicKey();
        bundle.put(Extra.ACCOUNT_ID, upload.getAccountId());
        bundle.put(Extra.METHOD, dest.getMethod());

        switch (upload.getDestination().getMethod()) {
            case Method.DOCUMENT:
                if (dest.getOwnerId() < 0) {
                    bundle.put(Extra.GROUP_ID, Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_ALBUM:
                bundle.put(Extra.ALBUM_ID, dest.getId());
                if (dest.getOwnerId() < 0) {
                    bundle.put(Extra.GROUP_ID, Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_COMMENT:
            case Method.PHOTO_TO_WALL:
                if (dest.getOwnerId() < 0) {
                    bundle.put(Extra.GROUP_ID, Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_MESSAGE:
                //do nothink
                break;
            case Method.PHOTO_TO_PROFILE:
                bundle.put(Extra.OWNER_ID, dest.getOwnerId());
                break;
        }

        return bundle;
    }

    private static final class ServiceStub extends IUploadService.Stub {

        private final WeakReference<UploadService> mService;

        private ServiceStub(final UploadService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public UploadObject getCurrent() throws RemoteException {
            return mService.get().getCurrent();
        }
    }
}
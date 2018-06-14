package biz.dealnote.messenger.upload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.interfaces.IMessagesStorage;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.service.MessageSender;
import biz.dealnote.messenger.upload.task.AbstractUploadTask;
import biz.dealnote.messenger.upload.task.DocumentUploadTask;
import biz.dealnote.messenger.upload.task.OwnerPhotoUploadTask;
import biz.dealnote.messenger.upload.task.PhotoMessageUploadTask;
import biz.dealnote.messenger.upload.task.PhotoToAlbumTask;
import biz.dealnote.messenger.upload.task.PhotoWallUploadTask;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

public class UploadService extends Service implements UploadCallback {

    private static final String TAG = UploadService.class.getSimpleName();

    public static final String ACTION_UPLOAD_FIRST = BuildConfig.APPLICATION_ID + ".service.ACTION_UPLOAD_FIRST";
    public static final String ACTION_CANCEL_BY_DESTINATION = BuildConfig.APPLICATION_ID + ".service.ACTION_CANCEL_BY_DESTINATION";
    public static final String ACTION_CANCEL_BY_ID = BuildConfig.APPLICATION_ID + ".service.ACTION_CANCEL_BY_ID";

    private final IBinder mBinder = new ServiceStub(this);

    private IUploadQueueStore uploadsRepository;
    private IMessagesStorage messagesStore;

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
                .subscribe(this::uploadFirstInQueue, ignore()));
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
                .subscribe(object -> cancelById(object.getId()), ignore()));
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
                }, ignore()));
    }

    private void startSendService() {
        MessageSender.getSendService().runSendingQueue();
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

        RxUtils.subscribeOnIOAndIgnore(uploadsRepository.removeWithId(upload.getId()));
        uploadFirstInQueue();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void fireStatusChange(UploadObject uploadObject) {
        RxUtils.subscribeOnIOAndIgnore(uploadsRepository.changeStatus(uploadObject.getId(), uploadObject.getStatus()));
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

        RxUtils.subscribeOnIOAndIgnore(uploadsRepository.removeWithId(id));
        return true;
    }

    private HashMap<String, UploadServer> mServersMap = new HashMap<>();

    private void saveServerToCache(UploadObject upload, UploadServer server) {
        mServersMap.put(createServerKey(upload), server);
    }

    private UploadServer findServerFor(UploadObject upload) {
        String key = createServerKey(upload);
        UploadServer targetServer = mServersMap.get(key);

        Logger.d(TAG, "Try to re-use server, from cache: " + (targetServer == null ? "null" : targetServer.getUrl()) + ", key: " + key);
        return targetServer;
    }

    private static String createServerKey(UploadObject upload) {
        UploadDestination dest = upload.getDestination();

        StringBuilder builder = new StringBuilder();
        builder.append(Extra.ACCOUNT_ID).append(upload.getAccountId());
        builder.append(Extra.METHOD).append(dest.getMethod());

        switch (upload.getDestination().getMethod()) {
            case Method.DOCUMENT:
                if (dest.getOwnerId() < 0) {
                    builder.append(Extra.GROUP_ID).append(Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_ALBUM:
                builder.append(Extra.ALBUM_ID).append(dest.getId());
                if (dest.getOwnerId() < 0) {
                    builder.append(Extra.GROUP_ID).append(Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_COMMENT:
            case Method.PHOTO_TO_WALL:
                if (dest.getOwnerId() < 0) {
                    builder.append(Extra.GROUP_ID).append(Math.abs(dest.getOwnerId()));
                }
                break;
            case Method.PHOTO_TO_MESSAGE:
                //do nothink
                break;
            case Method.PHOTO_TO_PROFILE:
                builder.append(Extra.OWNER_ID).append(dest.getOwnerId());
                break;
        }

        return builder.toString();
    }

    private static final class ServiceStub extends IUploadService.Stub {

        private final WeakReference<UploadService> mService;

        private ServiceStub(final UploadService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public UploadObject getCurrent() {
            return mService.get().getCurrent();
        }
    }
}
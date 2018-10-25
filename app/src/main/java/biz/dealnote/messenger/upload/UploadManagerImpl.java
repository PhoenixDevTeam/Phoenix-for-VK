package biz.dealnote.messenger.upload;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.upload.impl.DocumentUploadable;
import biz.dealnote.messenger.upload.impl.OwnerPhotoUploadable;
import biz.dealnote.messenger.upload.impl.Photo2AlbumUploadable;
import biz.dealnote.messenger.upload.impl.Photo2MessageUploadable;
import biz.dealnote.messenger.upload.impl.Photo2WallUploadable;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class UploadManagerImpl implements IUploadManager {

    private static final int PROGRESS_LOOKUP_DELAY = 500;
    private static final String NOTIFICATION_CHANNEL_ID = "upload_files";

    private final Context context;
    private final INetworker networker;
    private final IStorages storages;
    private final IAttachmentsRepository attachmentsRepository;
    private final IWalls walls;
    private final SendService sendService;
    private final List<Upload> queue = new ArrayList<>();
    private final Scheduler scheduler;

    private final PublishProcessor<List<Upload>> addingProcessor = PublishProcessor.create();
    private final PublishProcessor<int[]> deletingProcessor = PublishProcessor.create();
    private final PublishProcessor<Pair<Upload, UploadResult<?>>> completeProcessor = PublishProcessor.create();
    private final PublishProcessor<Upload> statusProcessor = PublishProcessor.create();

    private final Flowable<Long> timer;
    private final CompositeDisposable notificationUpdateDisposable = new CompositeDisposable();
    private final Map<String, UploadServer> serverMap = Collections.synchronizedMap(new HashMap<>());
    private volatile Upload current;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CompositeDisposable otherDisposables = new CompositeDisposable();

    public UploadManagerImpl(Context context, INetworker networker, IStorages storages, IAttachmentsRepository attachmentsRepository,
                             IWalls walls, SendService sendService) {
        this.context = context.getApplicationContext();
        this.networker = networker;
        this.storages = storages;
        this.attachmentsRepository = attachmentsRepository;
        this.walls = walls;
        this.sendService = sendService;
        this.scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
        this.timer = Flowable.interval(PROGRESS_LOOKUP_DELAY, PROGRESS_LOOKUP_DELAY, TimeUnit.MILLISECONDS);
    }

    private static Upload intent2Upload(UploadIntent intent) {
        return new Upload(intent.getAccountId())
                .setAutoCommit(intent.isAutoCommit())
                .setDestination(intent.getDestination())
                .setFileId(intent.getFileId())
                .setFileUri(intent.getFileUri())
                .setStatus(Upload.STATUS_QUEUE)
                .setSize(intent.getSize());
    }

    private static String createServerKey(Upload upload) {
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

    @Override
    public Single<List<Upload>> get(int accountId, @NonNull UploadDestination destination) {
        return Single.fromCallable(() -> getByDestination(accountId, destination));
    }

    private List<Upload> getByDestination(int accountId, @NonNull UploadDestination destination) {
        synchronized (UploadManagerImpl.this) {
            List<Upload> data = new ArrayList<>();
            for (Upload upload : queue) {
                if (accountId == upload.getAccountId() && destination.compareTo(upload.getDestination())) {
                    data.add(upload);
                }
            }
            return data;
        }
    }

    private void startWithNotification() {
        updateNotification(Collections.emptyList());

        notificationUpdateDisposable.add(observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::updateNotification));
    }

    private boolean needCreateChannel = true;

    private void updateNotification(List<IProgressUpdate> updates) {
        if(nonEmpty(updates)){
            int progress = updates.get(0).getProgress();

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(isNull(notificationManager)){
                return;
            }

            final NotificationCompat.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(needCreateChannel){
                    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, context.getString(R.string.channel_upload_files), NotificationManager.IMPORTANCE_LOW);
                    notificationManager.createNotificationChannel(channel);
                    needCreateChannel = false;
                }

                builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            } else {
                builder = new NotificationCompat.Builder(context).setPriority(Notification.PRIORITY_LOW);
            }

            builder.setContentTitle(context.getString(R.string.files_uploading_notification_title))
                    .setSmallIcon(R.drawable.ic_notification_upload)
                    .setOngoing(true)
                    .setProgress(100, progress, false)
                    .build();

            notificationManager.notify(NotificationHelper.NOTIFICATION_UPLOAD, builder.build());
        }
    }

    private void stopNotification() {
        notificationUpdateDisposable.clear();
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nonNull(notificationManager)){
            notificationManager.cancel(NotificationHelper.NOTIFICATION_UPLOAD);
        }
    }

    @Override
    public void enqueue(@NonNull List<UploadIntent> intents) {
        synchronized (this) {
            List<Upload> all = new ArrayList<>(intents.size());

            for (UploadIntent intent : intents) {
                Upload upload = intent2Upload(intent);
                all.add(upload);
                queue.add(upload);
            }

            addingProcessor.onNext(all);
            startIfNotStarted();
        }
    }

    private void startIfNotStarted() {
        compositeDisposable.add(Completable.complete()
                .observeOn(scheduler)
                .subscribe(this::startIfNotStartedInternal));
    }

    private Upload findFirstQueue() {
        Upload first = null;
        for (Upload u : queue) {
            if (u.getStatus() == Upload.STATUS_QUEUE) {
                first = u;
                break;
            }
        }
        return first;
    }

    private void startIfNotStartedInternal() {
        synchronized (this) {
            final Upload first = findFirstQueue();
            if (current != null) return;

            if(first == null){
                stopNotification();
                return;
            }

            startWithNotification();

            this.current = first;

            first.setStatus(Upload.STATUS_UPLOADING).setErrorText(null);
            statusProcessor.onNext(first);

            final IUploadable<?> uploadable = createUploadable(first);
            final UploadServer server = serverMap.get(createServerKey(first));

            compositeDisposable.add(uploadable.doUpload(first, server, new WeakProgressPublisgher(first))
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe(result -> onUploadComplete(first, result), t -> onUploadFail(first, t)));
        }
    }

    private void onUploadComplete(Upload upload, UploadResult<?> result) {
        synchronized (this) {
            queue.remove(upload);

            if (current == upload) {
                current = null;
            }

            final int accountId = upload.getAccountId();
            final UploadDestination destination = upload.getDestination();
            if (destination.getMethod() == Method.PHOTO_TO_MESSAGE && getByDestination(accountId, destination).isEmpty()) {
                sendMessageIfWaitForUpload(accountId, destination.getId());
            }

            serverMap.put(createServerKey(upload), result.getServer());

            completeProcessor.onNext(Pair.Companion.create(upload, result));
            startIfNotStartedInternal();
        }
    }

    private void sendMessageIfWaitForUpload(int accountId, int messageId) {
        // если загружали в личное сообщение, то отправляем это сообщение (в случае, если оно с статусе "Ожидание загрузки")
        otherDisposables.add(storages.messages()
                .getMessageStatus(accountId, messageId)
                .flatMap(status -> {
                    if (status == MessageStatus.WAITING_FOR_UPLOAD) {
                        return storages.messages()
                                .changeMessageStatus(accountId, messageId, MessageStatus.QUEUE, null)
                                .andThen(Single.just(true));
                    }

                    return Single.just(false);
                })
                .subscribeOn(scheduler)
                .subscribe(needStart -> {
                    if (needStart) {
                        sendService.runSendingQueue();
                    }
                }, ignore()));
    }

    private void onUploadFail(Upload upload, Throwable t) {
        synchronized (this) {
            if (current == upload) {
                current = null;

                Throwable cause = getCauseIfRuntime(t);
                final String message = firstNonEmptyString(cause.getMessage(), cause.toString());
                compositeDisposable.add(Completable.complete()
                        .observeOn(Injection.provideMainThreadScheduler())
                        .subscribe(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()));

            }

            String errorMessage = firstNonEmptyString(t.getMessage(), t.toString());
            upload.setStatus(Upload.STATUS_ERROR).setErrorText(errorMessage);
            statusProcessor.onNext(upload);

            startIfNotStartedInternal();
        }
    }

    @Override
    public void cancel(int id) {
        synchronized (this) {
            if (current != null && current.getId() == id) {
                compositeDisposable.clear();
                current = null;
            }

            int index = Utils.findIndexById(queue, id);
            if (index != -1) {
                queue.remove(index);
                deletingProcessor.onNext(new int[]{id});
            }

            startIfNotStarted();
        }
    }

    @Override
    public void cancelAll(int accountId, @NonNull UploadDestination destination) {
        synchronized (this) {
            if (current != null && accountId == current.getAccountId() && destination.compareTo(current.getDestination())) {
                compositeDisposable.clear();
                current = null;
            }

            List<Upload> target = new ArrayList<>();

            Iterator<Upload> iterator = queue.iterator();
            while (iterator.hasNext()) {
                Upload next = iterator.next();
                if (accountId == next.getAccountId() && destination.compareTo(next.getDestination())) {
                    iterator.remove();
                    target.add(next);
                }
            }

            if (target.size() > 0) {
                int[] ids = new int[target.size()];
                for (int i = 0; i < target.size(); i++) {
                    ids[i] = target.get(i).getId();
                }
                deletingProcessor.onNext(ids);
            }

            startIfNotStarted();
        }
    }

    @Override
    public Optional<Upload> getCurrent() {
        synchronized (this) {
            return Optional.wrap(current);
        }
    }

    @Override
    public Flowable<int[]> observeDeleting(boolean includeCompleted) {
        if (includeCompleted) {
            Flowable<int[]> completeIds = completeProcessor.onBackpressureBuffer()
                    .map(pair -> new int[]{pair.getFirst().getId()});

            return Flowable.merge(deletingProcessor.onBackpressureBuffer(), completeIds);
        }

        return deletingProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<Upload>> observeAdding() {
        return addingProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<Upload> obseveStatus() {
        return statusProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<Pair<Upload, UploadResult<?>>> observeResults() {
        return completeProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<List<IProgressUpdate>> observeProgress() {
        return timer.map(ignored -> {
            synchronized (UploadManagerImpl.this) {
                if (current == null) {
                    return Collections.emptyList();
                }

                IProgressUpdate update = new ProgressUpdate(current.getId(), current.getProgress());
                return Collections.singletonList(update);
            }
        });
    }

    private IUploadable<?> createUploadable(Upload upload) {
        final UploadDestination destination = upload.getDestination();

        switch (destination.getMethod()) {
            case Method.PHOTO_TO_MESSAGE:
                return new Photo2MessageUploadable(context, networker, attachmentsRepository, storages.messages());
            case Method.PHOTO_TO_ALBUM:
                return new Photo2AlbumUploadable(context, networker, storages.photos());
            case Method.DOCUMENT:
                return new DocumentUploadable(context, networker, storages.docs());
            case Method.PHOTO_TO_COMMENT:
            case Method.PHOTO_TO_WALL:
                return new Photo2WallUploadable(context, networker, attachmentsRepository);
            case Method.PHOTO_TO_PROFILE:
                return new OwnerPhotoUploadable(context, networker, walls);
        }

        throw new UnsupportedOperationException();
    }

    private static final class WeakProgressPublisgher implements PercentagePublisher {

        final WeakReference<Upload> reference;

        WeakProgressPublisgher(Upload upload) {
            this.reference = new WeakReference<>(upload);
        }

        @Override
        public void onProgressChanged(int percentage) {
            Upload upload = reference.get();
            if (upload != null) {
                upload.setProgress(percentage);
            }
        }
    }

    private static final class ProgressUpdate implements IProgressUpdate {

        final int id;
        final int progress;

        private ProgressUpdate(int id, int progress) {
            this.id = id;
            this.progress = progress;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public int getProgress() {
            return progress;
        }
    }
}
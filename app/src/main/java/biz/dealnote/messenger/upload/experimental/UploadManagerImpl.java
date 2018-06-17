package biz.dealnote.messenger.upload.experimental;

import android.content.Context;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.PercentagePublisher;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
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

import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;

public class UploadManagerImpl implements IUploadManager {

    private static final int PROGRESS_LOOKUP_DELAY = 500;

    private final Context context;
    private final INetworker networker;
    private final IStorages storages;
    private final IAttachmentsRepository attachmentsRepository;
    private final IWalls walls;
    private final SendService sendService;
    private final List<UploadObject> queue = new ArrayList<>();
    private final Scheduler scheduler;

    private final PublishProcessor<List<UploadObject>> addingProcessor = PublishProcessor.create();
    private final PublishProcessor<int[]> deletingProcessor = PublishProcessor.create();
    private final PublishProcessor<Pair<UploadObject, UploadResult<?>>> completeProcessor = PublishProcessor.create();
    private final PublishProcessor<UploadObject> statusProcessor = PublishProcessor.create();

    private final Flowable<Long> timer;

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

    @Override
    public Single<List<UploadObject>> get(int accountId, @NonNull UploadDestination destination) {
        return Single.fromCallable(() -> getByDestination(accountId, destination));
    }

    private List<UploadObject> getByDestination(int accountId, @NonNull UploadDestination destination) {
        synchronized (UploadManagerImpl.this) {
            List<UploadObject> data = new ArrayList<>();
            for (UploadObject upload : queue) {
                if (accountId == upload.getAccountId() && destination.compareTo(upload.getDestination())) {
                    data.add(upload);
                }
            }
            return data;
        }
    }

    private static UploadObject intent2Object(UploadIntent intent) {
        return new UploadObject(intent.getAccountId())
                .setAutoCommit(intent.isAutoCommit())
                .setDestination(intent.getDestination())
                .setFileId(intent.getFileId())
                .setFileUri(intent.getFileUri())
                .setStatus(UploadObject.STATUS_QUEUE)
                .setSize(intent.getSize());
    }

    @Override
    public void enqueue(@NonNull List<UploadIntent> intents) {
        synchronized (this) {
            List<UploadObject> all = new ArrayList<>(intents.size());

            for (UploadIntent intent : intents) {
                UploadObject o = intent2Object(intent);
                all.add(o);
                queue.add(o);
            }

            addingProcessor.onNext(all);

            startIfNotStarted();
        }
    }

    private volatile UploadObject current;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private void startIfNotStarted() {
        compositeDisposable.add(Completable.complete()
                .observeOn(scheduler)
                .subscribe(this::startIfNotStartedInternal));
    }

    private UploadObject findFirstQueue() {
        UploadObject first = null;
        for (UploadObject u : queue) {
            if (u.getStatus() == UploadObject.STATUS_QUEUE) {
                first = u;
                break;
            }
        }
        return first;
    }

    private void startIfNotStartedInternal() {
        synchronized (this) {
            final UploadObject first = findFirstQueue();
            if (current != null || first == null) return;

            this.current = first;

            first.setStatus(UploadObject.STATUS_UPLOADING).setErrorText(null);
            statusProcessor.onNext(first);

            final IUploadable<?> uploadable = createUploadable(first);
            final UploadServer server = serverMap.get(createServerKey(first));

            compositeDisposable.add(uploadable.doUpload(first, server, new WeakProgressPublisgher(first))
                    .subscribeOn(scheduler)
                    .observeOn(scheduler)
                    .subscribe(result -> onUploadComplete(first, result), t -> onUploadFail(first, t)));
        }
    }

    private void onUploadComplete(UploadObject upload, UploadResult<?> result) {
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

            completeProcessor.onNext(Pair.create(upload, result));
            startIfNotStartedInternal();
        }
    }

    private CompositeDisposable otherDisposables = new CompositeDisposable();

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

    private static final class WeakProgressPublisgher implements PercentagePublisher {

        final WeakReference<UploadObject> reference;

        WeakProgressPublisgher(UploadObject upload) {
            this.reference = new WeakReference<>(upload);
        }

        @Override
        public void onProgressChanged(int percentage) {
            UploadObject upload = reference.get();
            if (upload != null) {
                upload.setProgress(percentage);
            }
        }
    }

    private void onUploadFail(UploadObject upload, Throwable t) {
        synchronized (this) {
            if (current == upload) {
                current = null;
            }

            if (BuildConfig.DEBUG) {
                t.printStackTrace();
            }

            String errorMessage = firstNonEmptyString(t.getMessage(), t.toString());
            upload.setStatus(UploadObject.STATUS_ERROR).setErrorText(errorMessage);
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

            List<UploadObject> target = new ArrayList<>();

            Iterator<UploadObject> iterator = queue.iterator();
            while (iterator.hasNext()) {
                UploadObject next = iterator.next();
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
    public Optional<UploadObject> getCurrent() {
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
    public Flowable<List<UploadObject>> observeAdding() {
        return addingProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<UploadObject> obseveStatus() {
        return statusProcessor.onBackpressureBuffer();
    }

    @Override
    public Flowable<Pair<UploadObject, UploadResult<?>>> observeResults() {
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

    private IUploadable<?> createUploadable(UploadObject upload) {
        final UploadDestination destination = upload.getDestination();

        switch (destination.getMethod()) {
            case Method.PHOTO_TO_MESSAGE:
                return new Photo2Message(context, networker, attachmentsRepository, storages.messages());
            case Method.PHOTO_TO_ALBUM:
                return new Photo2Album(context, networker, storages.photos());
            case Method.DOCUMENT:
                return new DocumentUploadable(context, networker, storages.docs());
            case Method.PHOTO_TO_COMMENT:
            case Method.PHOTO_TO_WALL:
                return new Photo2Wall(context, networker, attachmentsRepository);
            case Method.PHOTO_TO_PROFILE:
                return new OwnerPhotoUploadable(context, networker, walls);
        }

        throw new UnsupportedOperationException();
    }

    private final Map<String, UploadServer> serverMap = Collections.synchronizedMap(new HashMap<>());

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
}
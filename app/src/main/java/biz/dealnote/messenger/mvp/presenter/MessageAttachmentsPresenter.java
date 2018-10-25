package biz.dealnote.messenger.mvp.presenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IMessageAttachmentsView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.IUploadManager;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.FileUtil;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.findIndexByPredicate;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public class MessageAttachmentsPresenter extends RxSupportPresenter<IMessageAttachmentsView> {

    private final int accountId;
    private final int messageOwnerId;
    private final int messageId;
    private final List<AttachmenEntry> entries;

    private final IAttachmentsRepository attachmentsRepository;
    private final UploadDestination destination;

    private Uri currentPhotoCameraUri;
    private final IUploadManager uploadManager;

    public MessageAttachmentsPresenter(int accountId, int messageOwnerId, int messageId, @Nullable ModelsBundle bundle, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.accountId = accountId;
        this.messageId = messageId;
        this.messageOwnerId = messageOwnerId;
        this.destination = UploadDestination.forMessage(messageId);
        this.entries = new ArrayList<>();
        this.attachmentsRepository = Injection.provideAttachmentsRepository();
        this.uploadManager = Injection.provideUploadManager();

        if (nonNull(savedInstanceState)) {
            this.currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CAMERA_FILE_URI);
            ArrayList<AttachmenEntry> accompanying = savedInstanceState.getParcelableArrayList(SAVE_ACCOMPANYING_ENTRIES);
            AssertUtils.requireNonNull(accompanying);
            this.entries.addAll(accompanying);
        } else {
            handleInputModels(bundle);
        }

        Predicate<IAttachmentsRepository.IBaseEvent> predicate = event -> event.getAttachToType() == AttachToType.MESSAGE
                && event.getAttachToId() == messageId
                && event.getAccountId() == messageOwnerId;

        appendDisposable(attachmentsRepository
                .observeAdding()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onAttachmentsAdded(event.getAttachments())));

        appendDisposable(attachmentsRepository
                .observeRemoving()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onAttachmentRemoved(event.getGeneratedId())));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusChanges));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdates));

        loadData();
    }

    private void handleInputModels(ModelsBundle bundle) {
        if (isNull(bundle)) {
            return;
        }

        for (AbsModel model : bundle) {
            entries.add(new AttachmenEntry(true, model).setAccompanying(true));
        }
    }

    @OnGuiCreated
    private void resolveEmptyViewVisibility() {
        if (isGuiReady()) {
            getView().setEmptyViewVisible(entries.isEmpty());
        }
    }

    private void onUploadProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findUploadObjectIndex(update.getId());
            if (index != -1) {
                Upload upload = (Upload) entries.get(index).getAttachment();
                if (upload.getStatus() != Upload.STATUS_UPLOADING) {
                    // for uploading only
                    continue;
                }

                upload.setProgress(update.getProgress());
                callView(view -> view.changePercentageSmoothly(index, update.getProgress()));
            }
        }
    }

    private void onUploadStatusChanges(Upload upload) {
        int index = findUploadObjectIndex(upload.getId());
        if (index != -1) {
            ((Upload) entries.get(index).getAttachment())
                    .setStatus(upload.getStatus())
                    .setErrorText(upload.getErrorText());

            callView(view -> view.notifyItemChanged(index));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findUploadObjectIndex(id);
            if (index != -1) {
                entries.remove(index);
                callView(view -> view.notifyEntryRemoved(index));
                resolveEmptyViewVisibility();
            }
        }
    }

    private void onUploadsAdded(List<Upload> uploads) {
        int count = 0;
        for (int i = uploads.size() - 1; i >= 0; i--) {
            Upload upload = uploads.get(i);
            if (this.destination.compareTo(upload.getDestination())) {
                AttachmenEntry entry = new AttachmenEntry(true, upload);
                entries.add(0, entry);
                count++;
            }
        }

        int finalCount = count;
        callView(view -> view.notifyDataAdded(0, finalCount));
        resolveEmptyViewVisibility();
    }

    private int findUploadObjectIndex(int id) {
        return findIndexByPredicate(entries, entry -> {
            AbsModel model = entry.getAttachment();
            return model instanceof Upload && ((Upload) model).getId() == id;
        });
    }

    private void onAttachmentRemoved(int optionId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getOptionalId() == optionId) {
                entries.remove(i);
                int finalI = i;
                callView(view -> view.notifyEntryRemoved(finalI));
                break;
            }
        }

        resolveEmptyViewVisibility();
    }

    private static List<AttachmenEntry> entities2entries(List<Pair<Integer, AbsModel>> pairs) {
        List<AttachmenEntry> entries = new ArrayList<>(pairs.size());
        for (Pair<Integer, AbsModel> pair : pairs) {
            entries.add(new AttachmenEntry(true, pair.getSecond())
                    .setOptionalId(pair.getFirst()));
        }
        return entries;
    }

    private void onAttachmentsAdded(List<Pair<Integer, AbsModel>> pairs) {
        onDataReceived(entities2entries(pairs));
    }

    private void loadData() {
        appendDisposable(createLoadAllSingle()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, RxUtils.ignore()));
    }

    private Single<List<AttachmenEntry>> createLoadAllSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(messageOwnerId, AttachToType.MESSAGE, messageId)
                .map(MessageAttachmentsPresenter::entities2entries)
                .zipWith(uploadManager.get(messageOwnerId, destination), (atts, uploads) -> {
                    List<AttachmenEntry> data = new ArrayList<>(atts.size() + uploads.size());
                    for (Upload u : uploads) {
                        data.add(new AttachmenEntry(true, u));
                    }

                    data.addAll(atts);
                    return data;
                });
    }

    private void onDataReceived(List<AttachmenEntry> data) {
        if (data.isEmpty()) {
            return;
        }

        int startCount = this.entries.size();
        this.entries.addAll(data);

        resolveEmptyViewVisibility();
        callView(view -> view.notifyDataAdded(startCount, data.size()));
    }

    @Override
    public void onGuiCreated(@NonNull IMessageAttachmentsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayAttachments(entries);
    }

    public void fireAddPhotoButtonClick() {
        // Если сообщения группы - предлагать фотографии сообщества, а не группы
        getView().addPhoto(accountId, messageOwnerId);
    }

    public void firePhotosSelected(ArrayList<Photo> photos, ArrayList<LocalPhoto> localPhotos) {
        if (nonEmpty(photos)) {
            fireAttachmentsSelected(photos);
        } else if (nonEmpty(localPhotos)) {
            doUploadPhotos(localPhotos);
        }
    }

    private void doUploadPhotos(List<LocalPhoto> photos) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (isNull(size)) {
            getView().displaySelectUploadPhotoSizeDialog(photos);
        } else {
            doUploadPhotos(photos, size);
        }
    }

    private void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(messageOwnerId, destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void fireRemoveClick(AttachmenEntry entry) {
        if (entry.getOptionalId() != 0) {
            RxUtils.subscribeOnIOAndIgnore(attachmentsRepository.remove(messageOwnerId, AttachToType.MESSAGE, messageId, entry.getOptionalId()));
            return;
        }

        if (entry.getAttachment() instanceof Upload) {
            uploadManager.cancel(((Upload) entry.getAttachment()).getId());
            return;
        }

        if (entry.isAccompanying()) {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).getId() == entry.getId()) {
                    entries.remove(i);
                    getView().notifyEntryRemoved(i);
                    syncAccompanyingWithParent();
                    break;
                }
            }
        }
    }

    public void fireUploadPhotoSizeSelected(List<LocalPhoto> photos, int imageSize) {
        doUploadPhotos(photos, imageSize);
    }

    public void fireCameraPermissionResolved() {
        if (AppPerms.hasCameraPermision(getApplicationContext())) {
            makePhotoInternal();
        }
    }

    public void fireButtonCameraClick() {
        if (AppPerms.hasCameraPermision(getApplicationContext())) {
            makePhotoInternal();
        } else {
            getView().requestCameraPermission();
        }
    }

    private void makePhotoInternal() {
        try {
            File file = FileUtil.createImageFile();
            this.currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), file);
            getView().startCamera(currentPhotoCameraUri);
        } catch (IOException e) {
            safeShowError(getView(), e.getMessage());
        }
    }

    private static final String SAVE_CAMERA_FILE_URI = "save_camera_file_uri";
    private static final String SAVE_ACCOMPANYING_ENTRIES = "save_accompanying_entries";

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CAMERA_FILE_URI, currentPhotoCameraUri);

        // сохраняем в outState только неПерсистентные данные
        ArrayList<AttachmenEntry> accompanying = new ArrayList<>();
        for (AttachmenEntry entry : entries) {
            if (entry.isAccompanying()) {
                accompanying.add(entry);
            }
        }

        outState.putParcelableArrayList(SAVE_ACCOMPANYING_ENTRIES, accompanying);
    }

    private void syncAccompanyingWithParent() {
        ModelsBundle bundle = new ModelsBundle();
        for (AttachmenEntry entry : entries) {
            if (entry.isAccompanying()) {
                bundle.append(entry.getAttachment());
            }
        }

        getView().syncAccompanyingWithParent(bundle);
    }

    public void firePhotoMaked() {
        final Uri uri = this.currentPhotoCameraUri;
        this.currentPhotoCameraUri = null;

        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        getApplicationContext().sendBroadcast(scanIntent);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(uri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    public void fireButtonVideoClick() {
        getView().startAddVideoActivity(accountId, messageOwnerId);
    }

    public void fireButtonDocClick() {
        getView().startAddDocumentActivity(accountId); // TODO: 16.08.2017
    }

    public void fireAttachmentsSelected(ArrayList<? extends AbsModel> attachments) {
        RxUtils.subscribeOnIOAndIgnore(attachmentsRepository.attach(messageOwnerId, AttachToType.MESSAGE, messageId, attachments));
    }
}
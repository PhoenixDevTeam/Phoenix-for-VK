package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.mvp.view.ICreateCommentView;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Predicate;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.removeIf;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 27.03.2017.
 * phoenix
 */
public class CommentCreatePresenter extends AbsAttachmentsEditPresenter<ICreateCommentView> {

    private static final String TAG = CommentCreatePresenter.class.getSimpleName();

    private final int commentId;
    private final UploadDestination destination;
    private final IUploadQueueStore uploads;

    private final IAttachmentsRepository attachmentsRepository;

    public CommentCreatePresenter(int accountId, int commentDbid, int sourceOwnerId, String body, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.attachmentsRepository = Injection.provideAttachmentsRepository();
        this.uploads = Stores.getInstance().uploads();
        this.commentId = commentDbid;
        this.destination = UploadDestination.forComment(commentId, sourceOwnerId);

        if (isNull(savedInstanceState)) {
            setTextBody(body);
        }

        Predicate<UploadObject> predicate = o -> destination.compareTo(o.getDestination());

        appendDisposable(uploads.observeQueue()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, predicate)));

        appendDisposable(uploads.observeStatusUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploads.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(attachmentsRepository.observeAdding()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentsAdding));

        appendDisposable(attachmentsRepository.observeRemoving()
                .filter(this::filterAttachEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::handleAttachmentRemoving));

        loadAttachments();
    }

    private boolean filterAttachEvents(IAttachmentsRepository.IBaseEvent event) {
        return event.getAccountId() == getAccountId()
                && event.getAttachToId() == commentId
                && event.getAttachToType() == AttachToType.COMMENT;
    }

    private void handleAttachmentRemoving(IAttachmentsRepository.IRemoveEvent event) {
        if (removeIf(getData(), attachment -> attachment.getOptionalId() == event.getGeneratedId())) {
            safeNotifyDataSetChanged();
        }
    }

    private void handleAttachmentsAdding(IAttachmentsRepository.IAddEvent event) {
        addAll(event.getAttachments());
    }

    private void addAll(List<Pair<Integer, AbsModel>> data) {
        for (Pair<Integer, AbsModel> pair : data) {
            getData().add(new AttachmenEntry(true, pair.getSecond()).setOptionalId(pair.getFirst()));
        }

        if (safeCountOf(data) > 0) {
            safeNotifyDataSetChanged();
        }
    }

    private Single<List<AttachmenEntry>> attachmentsSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(getAccountId(), AttachToType.COMMENT, commentId)
                .map(pairs -> createFrom(pairs, true));
    }

    private Single<List<AttachmenEntry>> uploadsSingle() {
        return uploads.getAll(uploadObject -> destination.compareTo(uploadObject.getDestination()))
                .map(AbsAttachmentsEditPresenter::createFrom);
    }

    private void loadAttachments() {
        appendDisposable(attachmentsSingle()
                .zipWith(uploadsSingle(), this::combine)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAttachmentsRestored, Analytics::logUnexpectedError));
    }

    private void onAttachmentsRestored(List<AttachmenEntry> entries) {
        getData().addAll(entries);

        if (nonEmpty(entries)) {
            safeNotifyDataSetChanged();
        }
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getOptionalId() != 0) {
            attachmentsRepository.remove(getAccountId(), AttachToType.COMMENT, commentId, attachment.getOptionalId())
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {}, Analytics::logUnexpectedError);
            // из списка не удаляем, так как удаление из репозитория "слушается"
            // (будет удалено асинхронно и после этого удалится из списка)
        } else {
            // такого в комментах в принципе быть не может !!!
            manuallyRemoveElement(index);
        }
    }

    @Override
    protected void onModelsAdded(List<? extends AbsModel> models) {
        this.attachmentsRepository.attach(getAccountId(), AttachToType.COMMENT, commentId, models)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, Analytics::logUnexpectedError);
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        UploadUtils.upload(getApplicationContext(), UploadUtils.createIntents(getAccountId(), destination, photos, size, true));
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnGuiCreated
    private void resolveButtonsVisibility() {
        if (isGuiReady()) {
            getView().setSupportedButtons(true, true, true, true, false, false);
        }
    }

    private void returnDataToParent(){
        getView().returnDataToParent(getTextBody());
    }

    public void fireReadyClick() {
        getView().goBack();
    }

    public boolean onBackPressed() {
        returnDataToParent();
        return true;
    }
}

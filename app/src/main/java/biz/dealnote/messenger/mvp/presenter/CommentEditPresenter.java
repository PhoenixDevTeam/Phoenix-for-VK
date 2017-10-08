package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.domain.ICommentsInteractor;
import biz.dealnote.messenger.domain.impl.CommentsInteractor;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.mvp.view.ICommentEditView;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.upload.task.PhotoWallUploadTask;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 06.05.2017.
 * phoenix
 */
public class CommentEditPresenter extends AbsAttachmentsEditPresenter<ICommentEditView> {

    private static final String TAG = CommentEditPresenter.class.getSimpleName();

    private final Comment orig;
    private final UploadDestination destination;
    private boolean editingNow;
    private boolean canGoBack;

    private final ICommentsInteractor commentsInteractor;

    public CommentEditPresenter(Comment comment, int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.commentsInteractor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
        this.orig = comment;
        this.destination = new UploadDestination(comment.getId(), comment.getCommented().getSourceOwnerId(), Method.PHOTO_TO_COMMENT);

        IUploadQueueStore uploadRepository = Injection.provideStores().uploads();

        if (isNull(savedInstanceState)) {
            super.setTextBody(orig.getText());
            initialPopulateEntries();
        }

        appendDisposable(uploadRepository.getByDestination(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsReceived));

        appendDisposable(uploadRepository.observeQueue()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsQueueChanged));

        appendDisposable(uploadRepository.observeStatusUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadRepository.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));
    }

    private void onUploadsQueueChanged(List<IUploadQueueStore.IQueueUpdate> updates) {
        boolean hasChanges = false;

        for (IUploadQueueStore.IQueueUpdate u : updates) {
            if (u.isAdding()) {
                UploadObject object = u.object();
                AssertUtils.requireNonNull(object);

                if (!destination.equals(object.getDestination())) {
                    continue;
                }

                getData().add(new AttachmenEntry(true, object));
                hasChanges = true;
            } else {
                int index = findUploadIndexById(u.getId());

                if (nonNull(u.response())) {
                    PhotoWallUploadTask.Response response = (PhotoWallUploadTask.Response) u.response();
                    AssertUtils.requireNonNull(response);

                    AttachmenEntry entry = new AttachmenEntry(true, response.photo);

                    if (index != -1) {
                        getData().set(index, entry);
                    } else {
                        getData().add(0, entry);
                    }

                    hasChanges = true;
                } else if (index != -1) {
                    getData().remove(index);
                    hasChanges = true;
                }
            }
        }

        if (hasChanges) {
            safeNotifyDataSetChanged();
        }
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        super.manuallyRemoveElement(index);
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, false);
        UploadUtils.upload(getApplicationContext(), intents);
    }

    private void onUploadsReceived(List<UploadObject> uploads) {
        getData().addAll(createFrom(uploads));
        safeNotifyDataSetChanged();
    }

    @Override
    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        // сохраняем все, кроме аплоада
        return Utils.copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof UploadObject));
    }

    @OnGuiCreated
    private void resolveButtonsAvailability() {
        if (isGuiReady()) {
            getView().setSupportedButtons(true, false, true, true, false, false);
        }
    }

    private void initialPopulateEntries() {
        if (nonNull(orig.getAttachments())) {
            List<AbsModel> models = orig.getAttachments().toList();

            for (AbsModel m : models) {
                getData().add(new AttachmenEntry(true, m));
            }
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireReadyClick() {
        if (hasUploads()) {
            safeShowError(getView(), R.string.upload_not_resolved_exception_message);
            return;
        }

        List<AbsModel> models = new ArrayList<>();
        for (AttachmenEntry entry : super.getData()) {
            models.add(entry.getAttachment());
        }

        setEditingNow(true);

        final int accountId = super.getAccountId();
        final Commented commented = this.orig.getCommented();
        final int commentId = this.orig.getId();
        final String body = super.getTextBody();

        appendDisposable(commentsInteractor.edit(accountId, commented, commentId, body, models)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onEditComplete, this::onEditError));
    }

    private void onEditError(Throwable t) {
        setEditingNow(false);
        showError(getView(), t);
    }

    private void onEditComplete(@Nullable Comment comment) {
        setEditingNow(false);

        this.canGoBack = true;

        callView(view -> view.goBackWithResult(comment));
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (editingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.saving, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    public boolean onBackPressed() {
        if (canGoBack) {
            return true;
        }

        getView().showConfirmWithoutSavingDialog();
        return false;
    }

    public void fireSavingCancelClick() {
        UploadUtils.cancelByDestination(getApplicationContext(), destination);
        this.canGoBack = true;

        getView().goBack();
    }
}
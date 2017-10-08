package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.db.AttachToType;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Attachments;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.WallEditorAttrs;
import biz.dealnote.messenger.mvp.view.IPostCreateView;
import biz.dealnote.messenger.upload.Method;
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
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.copyToArrayListWithPredicate;
import static biz.dealnote.messenger.util.Utils.findInfoByPredicate;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 20.01.2017.
 * phoenix
 */
public class PostCreatePresenter extends AbsPostEditPresenter<IPostCreateView> {

    private static final String TAG = PostCreatePresenter.class.getSimpleName();

    private final int ownerId;

    @EditingPostType
    private final int editingType;

    private Post post;
    private boolean postPublished;
    private final WallEditorAttrs attrs;

    private final IAttachmentsRepository attachmentsRepository;
    private final IUploadQueueStore uploadsRepository;
    private final IWalls walls;

    public PostCreatePresenter(int accountId, int ownerId, @EditingPostType int editingType,
                               ModelsBundle bundle, @NonNull WallEditorAttrs attrs, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.attachmentsRepository = Injection.provideAttachmentsRepository();
        this.uploadsRepository = Injection.provideStores().uploads();
        this.walls = Injection.provideWalls();

        this.attrs = attrs;
        this.ownerId = ownerId;
        this.editingType = editingType;

        if (isNull(savedInstanceState)) {
            if (nonNull(bundle)) {
                for (AbsModel i : bundle) {
                    getData().add(new AttachmenEntry(false, i));
                }
            }
        }

        setupAttachmentsListening();
        setupUploadListening();

        restoreEditingWallPostFromDbAsync();

        // только на моей стене
        setFriendsOnlyOptionAvailable(ownerId > 0 && ownerId == accountId);

        // доступно только в группах и только для редакторов и выше
        setFromGroupOptionAvailable(isGroup() && isEditorOrHigher());

        // доступно только для публичных страниц(и я одмен) или если нажат "От имени группы"
        setAddSignatureOptionAvailable((isCommunity() && isEditorOrHigher()) || fromGroup.get());
    }

    @Override
    public void onGuiCreated(@NonNull IPostCreateView view) {
        super.onGuiCreated(view);

        @StringRes
        int toolbarTitleRes = isCommunity() && !isEditorOrHigher() ? R.string.title_suggest_news : R.string.title_activity_create_post;
        view.setToolbarTitle(getString(toolbarTitleRes));
        view.setToolbarSubtitle(getOwner().getFullName());
    }

    @Override
    void onFromGroupChecked(boolean checked) {
        super.onFromGroupChecked(checked);

        setAddSignatureOptionAvailable(checked);
        resolveSignerInfo();
    }

    @Override
    void onShowAuthorChecked(boolean checked) {
        resolveSignerInfo();
    }

    @OnGuiCreated
    private void resolveSignerInfo() {
        if (isGuiReady()) {
            boolean visible = false;

            if (isGroup()) {
                if (!isEditorOrHigher()) {
                    visible = true;
                } else if (!fromGroup.get()) {
                    visible = true;
                } else if (addSignature.get()) {
                    visible = true;
                }
            }

            if (isCommunity() && isEditorOrHigher()) {
                visible = addSignature.get();
            }

            Owner author = getAuthor();

            getView().displaySignerInfo(author.getFullName(), author.get100photoOrSmaller());
            getView().setSignerInfoVisible(visible);
        }
    }

    private Owner getAuthor() {
        return attrs.getEditor();
    }

    private boolean isEditorOrHigher() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.EDITOR;
    }

    private boolean isGroup() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getType() == VKApiCommunity.Type.GROUP;
    }

    private boolean isCommunity() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getType() == VKApiCommunity.Type.PAGE;
    }

    @Override
    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        Predicate<AttachmenEntry> predicate = entry -> {
            // сохраняем только те, что не лежат в базе
            AbsModel model = entry.getAttachment();
            return !(model instanceof UploadObject) && entry.getOptionalId() == 0;
        };

        return copyToArrayListWithPredicate(getData(), predicate);
    }

    private void setupAttachmentsListening() {
        appendDisposable(attachmentsRepository.observeAdding()
                .filter(this::filterAttachmentEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onRepositoryAttachmentsAdded(event.getAttachments())));

        appendDisposable(attachmentsRepository.observeRemoving()
                .filter(this::filterAttachmentEvents)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onRepositoryAttachmentsRemoved));
    }

    private void setupUploadListening() {
        appendDisposable(uploadsRepository.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(uploadsRepository.observeStatusUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadsRepository.observeQueue()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, this::isUploadToThis), Analytics::logUnexpectedError));
    }

    private boolean isUploadToThis(UploadObject upload) {
        UploadDestination dest = upload.getDestination();
        return nonNull(post)
                && dest.getMethod() == Method.PHOTO_TO_WALL
                && dest.getOwnerId() == ownerId
                && dest.getId() == post.getDbid();
    }

    private void restoreEditingWallPostFromDbAsync() {
        appendDisposable(walls
                .getEditingPost(getAccountId(), ownerId, editingType, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPostRestored, Analytics::logUnexpectedError));
    }

    private void restoreEditingAttachmentsAsync() {
        appendDisposable(attachmentsSingle()
                .zipWith(uploadsSingle(), this::combine)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAttachmentsRestored, Analytics::logUnexpectedError));
    }

    private void onPostRestored(Post post) {
        this.post = post;
        super.checkFriendsOnly(post.isFriendsOnly());

        boolean postpone = post.getPostType() == VKApiPost.Type.POSTPONE;
        setTimerValue(postpone ? post.getDate() : null);

        setTextBody(post.getText());

        restoreEditingAttachmentsAsync();
    }

    private Single<List<AttachmenEntry>> attachmentsSingle() {
        return attachmentsRepository
                .getAttachmentsWithIds(getAccountId(), AttachToType.POST, post.getDbid())
                .map(pairs -> createFrom(pairs, true));
    }

    private Single<List<AttachmenEntry>> uploadsSingle() {
        return uploadsRepository
                .getAll(this::isUploadToThis)
                .map(AbsAttachmentsEditPresenter::createFrom);
    }

    private void onAttachmentsRestored(List<AttachmenEntry> data) {
        if (nonEmpty(data)) {
            int size = getData().size();

            getData().addAll(data);

            safelyNotifyItemsAdded(size, data.size());
        }
    }

    private void onRepositoryAttachmentsRemoved(IAttachmentsRepository.IRemoveEvent event) {
        Pair<Integer, AttachmenEntry> info = findInfoByPredicate(getData(), entry -> entry.getOptionalId() == event.getGeneratedId());

        if (nonNull(info)) {
            AttachmenEntry entry = info.getSecond();
            int index = info.getFirst();

            getData().remove(index);
            safelyNotifyItemRemoved(index);

            if (entry.getAttachment() instanceof Poll) {
                resolveSupportButtons();
            }
        }
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        if (isNull(post)) {
            return;
        }

        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        UploadUtils.upload(getApplicationContext(), UploadUtils.createIntents(getAccountId(), destination, photos, size, true));
    }

    private boolean filterAttachmentEvents(IAttachmentsRepository.IBaseEvent event) {
        return nonNull(post)
                && event.getAttachToType() == AttachToType.POST
                && event.getAccountId() == getAccountId()
                && event.getAttachToId() == post.getDbid();
    }

    private void onRepositoryAttachmentsAdded(List<Pair<Integer, AbsModel>> data) {
        boolean pollAdded = false;

        int size = getData().size();

        for (Pair<Integer, AbsModel> pair : data) {
            AbsModel model = pair.getSecond();
            if (model instanceof Poll) {
                pollAdded = true;
            }

            getData().add(new AttachmenEntry(true, model)
                    .setOptionalId(pair.getFirst()));
        }

        safelyNotifyItemsAdded(size, data.size());

        if (pollAdded) {
            resolveSupportButtons();
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected void onPollCreateClick() {
        getView().openPollCreationWindow(getAccountId(), ownerId);
    }

    @Override
    protected void onModelsAdded(List<? extends AbsModel> models) {
        attachmentsRepository.attach(getAccountId(), AttachToType.POST, post.getDbid(), models)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, Analytics::logUnexpectedError);
    }

    @Override
    protected void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getOptionalId() != 0) {
            attachmentsRepository.remove(getAccountId(), AttachToType.POST, post.getDbid(), attachment.getOptionalId())
                    .subscribeOn(Schedulers.io())
                    .subscribe(() -> {}, Analytics::logUnexpectedError);
        } else {
            manuallyRemoveElement(index);
        }
    }

    @Override
    protected void onTimerClick() {
        if (post.getPostType() == VKApiPost.Type.POSTPONE) {
            post.setPostType(VKApiPost.Type.POST);
            setTimerValue(null);
            resolveTimerInfoView();
            return;
        }

        long initialTime = post.getDate() == 0 ? System.currentTimeMillis() / 1000 + 2 * 60 * 60 : post.getDate();
        getView().showEnterTimeDialog(initialTime);
    }

    public void fireTimerTimeSelected(long unixtime) {
        post.setPostType(VKApiPost.Type.POSTPONE);
        post.setDate(unixtime);

        setTimerValue(unixtime);
    }

    @OnGuiCreated
    private void resolveSupportButtons() {
        if (isGuiReady()) {
            getView().setSupportedButtons(true, true, true, true, isPollSupported(), isSupportTimer());
        }
    }

    private boolean isPollSupported() {
        for (AttachmenEntry entry : getData()) {
            if (entry.getAttachment() instanceof Poll) {
                return false;
            }
        }

        return true;
    }

    private Owner getOwner() {
        return attrs.getOwner();
    }

    private boolean isSupportTimer() {
        if (ownerId > 0) {
            return getAccountId() == ownerId;
        } else {
            return isEditorOrHigher();
        }
    }

    private boolean publishingNow;

    private void changePublishingNowState(boolean publishing) {
        this.publishingNow = publishing;
        resolvePublishDialogVisibility();
    }

    @OnGuiCreated
    private void resolvePublishDialogVisibility() {
        if (isGuiReady()) {
            if (publishingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    private void commitDataToPost() {
        if (isNull(post.getAttachments())) {
            post.setAttachments(new Attachments());
        }

        for (AttachmenEntry entry : getData()) {
            post.getAttachments().add(entry.getAttachment());
        }

        post.setText(getTextBody());
        post.setFriendsOnly(super.friendsOnly.get());
    }

    public void fireReadyClick() {
        appendDisposable(uploadsRepository
                .getAll(this::isUploadToThis)
                .map(List::size)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(count -> {
                    if (count > 0) {
                        safeShowError(getView(), R.string.wait_until_file_upload_is_complete);
                    } else {
                        doPost();
                    }
                }, Analytics::logUnexpectedError));
    }

    private void doPost() {
        commitDataToPost();

        changePublishingNowState(true);

        final boolean fromGroup = super.fromGroup.get();
        final boolean showSigner = super.addSignature.get();
        final int accountId = super.getAccountId();

        appendDisposable(walls
                .post(accountId, post, fromGroup, showSigner)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPostPublishSuccess, this::onPostPublishError));
    }

    @SuppressWarnings("unused")
    private void onPostPublishSuccess(Post post){
        changePublishingNowState(false);

        this.postPublished = true;

        getView().goBack();
    }

    private void onPostPublishError(Throwable t){
        changePublishingNowState(false);
        showError(getView(), getCauseIfRuntime(t));
    }

    private void releasePostDataAsync() {
        if (isNull(post)) {
            return;
        }

        UploadDestination destination = UploadDestination.forPost(post.getDbid(), ownerId);
        UploadUtils.cancelByDestination(getApplicationContext(), destination);

        walls.deleteFromCache(getAccountId(), post.getDbid())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, Analytics::logUnexpectedError);
    }

    private void safeDraftAsync() {
        commitDataToPost();

        final int accountId = getAccountId();

        walls.cachePostWithIdSaving(accountId, post)
                .subscribeOn(Schedulers.io())
                .subscribe(integer -> {}, Analytics::logUnexpectedError);
    }

    public boolean onBackPresed() {
        if (postPublished) {
            return true;
        }

        if (EditingPostType.TEMP == editingType) {
            releasePostDataAsync();
        } else {
            safeDraftAsync();
        }

        return true;
    }
}

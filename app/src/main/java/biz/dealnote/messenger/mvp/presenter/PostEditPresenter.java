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
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.domain.IWalls;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.WallEditorAttrs;
import biz.dealnote.messenger.mvp.view.IPostEditView;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.upload.task.PhotoWallUploadTask;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Predicate;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Unixtime;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.applyCompletableIOToMainSchedulers;
import static biz.dealnote.messenger.util.Utils.copyToArrayListWithPredicate;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 30.01.2017.
 * phoenix
 */
public class PostEditPresenter extends AbsPostEditPresenter<IPostEditView> {

    private static final String TAG = PostEditPresenter.class.getSimpleName();

    private static final String SAVE_POST = "save_post";

    private final Post post;
    private final UploadDestination uploadDestination;
    private final Predicate<UploadObject> uploadPredicate;

    private final IWalls wallInteractor;

    private final WallEditorAttrs attrs;

    private boolean editingNow;
    private boolean canExit;

    public PostEditPresenter(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.wallInteractor = Injection.provideWalls();
        this.attrs = attrs;

        if (isNull(savedInstanceState)) {
            this.post = safelyClone(post);

            super.setTextBody(post.getText());

            if (post.getPostType() == VKApiPost.Type.POSTPONE) {
                super.setTimerValue(post.getDate());
            }

            if (nonNull(post.getAttachments())) {
                List<AbsModel> list = post.getAttachments().toList();

                for (AbsModel model : list) {
                    super.getData().add(new AttachmenEntry(true, model));
                }
            }

            if (post.hasCopyHierarchy()) {
                super.getData().add(0, new AttachmenEntry(false, post.getCopyHierarchy().get(0)));
            }
        } else {
            this.post = savedInstanceState.getParcelable(SAVE_POST);
        }

        Owner owner = getOwner();

        super.setFriendsOnlyOptionAvailable(owner.getOwnerId() > 0 && owner.getOwnerId() == accountId);
        checkFriendsOnly(post.isFriendsOnly());

        super.setAddSignatureOptionAvailable(canAddSignature());

        super.addSignature.setValue(post.getSignerId() > 0);
        super.setFromGroupOptionAvailable(false); // only for publishing

        this.uploadDestination = UploadDestination.forPost(post.getVkid(), post.getOwnerId());
        this.uploadPredicate = object -> object.getAccountId() == getAccountId()
                && object.getDestination().compareTo(uploadDestination);

        setupUploadListening();
    }

    @NonNull
    private Owner getOwner() {
        return attrs.getOwner();
    }

    private Owner getMe() {
        return attrs.getEditor();
    }

    @Override
    void onShowAuthorChecked(boolean checked) {
        super.onShowAuthorChecked(checked);
        resolveSignerInfoVisibility();
    }

    private boolean isEditorOrHigher() {
        Owner owner = getOwner();
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.EDITOR;
    }

    private boolean postIsSuggest() {
        return post.getPostType() == VKApiPost.Type.SUGGEST;
    }

    private boolean postIsMine() {
        if(post.getCreatorId() > 0 && post.getCreatorId() == getAccountId()){
            return true;
        }

        if(post.getSignerId() > 0 && post.getSignerId() == getAccountId()){
            return true;
        }

        return false;
    }

    private boolean supportSignerInfoDisplaying() {
        if (!isAddSignatureOptionAvailable()) {
            return false;
        }

        // потому что она может быть недоступна (signer == null)
        if (postIsSuggest() && !postIsMine()) {
            return true;
        }

        return nonNull(post.getCreator());
    }

    @Override
    public void onGuiCreated(@NonNull IPostEditView view) {
        super.onGuiCreated(view);

        @StringRes
        int titleRes = isPublishingSuggestPost() ? R.string.publication : R.string.editing;

        view.setToolbarTitle(getString(titleRes));
        view.setToolbarSubtitle(getOwner().getFullName());
    }

    private Owner getDisplayedSigner() {
        if (postIsSuggest()) {
            return post.getAuthor();
        }

        if (nonNull(post.getCreator())) {
            return post.getCreator();
        }

        return getMe();
    }

    @OnGuiCreated
    private void resolveSignerInfoVisibility() {
        if (isGuiReady()) {
            Owner signer = getDisplayedSigner();

            getView().displaySignerInfo(signer.getFullName(), signer.get100photoOrSmaller());
            getView().setSignerInfoVisible(supportSignerInfoDisplaying() && addSignature.get());
        }
    }

    @Override
    ArrayList<AttachmenEntry> getNeedParcelSavingEntries() {
        return copyToArrayListWithPredicate(getData(), entry -> !(entry.getAttachment() instanceof UploadObject));
    }

    private static Post safelyClone(Post post) {
        try {
            return post.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Unable to clone post");
        }
    }

    private void setupUploadListening() {
        IUploadQueueStore repository = Stores.getInstance().uploads();

        appendDisposable(repository.observeQueue()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(updates -> onUploadQueueUpdates(updates, uploadPredicate)));

        appendDisposable(repository.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        appendDisposable(repository.observeStatusUpdates()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));
    }

    @Override
    boolean onUploadObjectRemovedFromQueue(int id, @Nullable BaseUploadResponse response) {
        int index = findUploadIndexById(id);
        if (index == -1) {
            return false;
        }

        if (response instanceof PhotoWallUploadTask.Response) {
            Photo photo = ((PhotoWallUploadTask.Response) response).photo;
            if (nonNull(photo)) {
                getData().set(index, new AttachmenEntry(true, photo));
                return true;
            }
        }

        super.getData().remove(index);
        return true;
    }

    private void setEditingNow(boolean editingNow) {
        this.editingNow = editingNow;
        resolveProgressDialog();
    }

    @OnGuiCreated
    private void resolveProgressDialog() {
        if (isGuiReady()) {
            if (editingNow) {
                getView().displayProgressDialog(R.string.please_wait, R.string.publication, false);
            } else {
                getView().dismissProgressDialog();
            }
        }
    }

    private boolean isPublishingSuggestPost() {
        // если пост предложенный - постим, если нет - редактируем
        // если пост мой и он предложенный - редактируем
        return postIsSuggest() && !postIsMine();
    }

    private void save() {
        Logger.d(TAG, "save, author: " + post.getAuthor() + ", signer: " + post.getCreator());

        appendDisposable(Stores.getInstance()
                .uploads()
                .getByDestination(getAccountId(), uploadDestination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> {
                    if (data.isEmpty()) {
                        doCommitImpl();
                    } else {
                        safeShowLongToast(getView(), R.string.wait_until_file_upload_is_complete);
                    }
                }, Analytics::logUnexpectedError));
    }

    private boolean isGroup() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.GROUP;
    }

    private boolean isCommunity() {
        return getOwner() instanceof Community && ((Community) getOwner()).getType() == VKApiCommunity.Type.PAGE;
    }

    private boolean canAddSignature() {
        if (!isEditorOrHigher()) {
            // только редакторы и выше могу указывать автора
            return false;
        }

        if (isGroup()) {
            // если группа - то только, если пост от имени группы
            return post.getAuthor() instanceof Community;
        }

        if (isCommunity()) {
            // в публичных страницах всегда можно
            return true;
        }

        return false;
    }

    private void doCommitImpl() {
        if (isPublishingSuggestPost()) {
            postImpl();
            return;
        }

        boolean timerCancelled = post.getPostType() == VKApiPost.Type.POSTPONE && isNull(getTimerValue());

        if (timerCancelled) {
            postImpl();
            return;
        }

        saveImpl();
    }

    private void saveImpl() {
        Long publishDate = getTimerValue();

        setEditingNow(true);
        final Boolean signed = canAddSignature() ? super.addSignature.get() : null;
        final Boolean friendsOnly = isFriendsOnlyOptionAvailable() ? super.friendsOnly.get() : null;

        appendDisposable(wallInteractor
                .editPost(getAccountId(), post.getOwnerId(), post.getVkid(), friendsOnly,
                        getTextBody(), getAttachmentTokens(), null, signed, publishDate,
                        null, null, null, null)
                .compose(applyCompletableIOToMainSchedulers())
                .subscribe(this::onEditResponse, throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void postImpl() {
        final int accountId = getAccountId();
        final Long publishDate = getTimerValue();
        final String body = getTextBody();
        final Boolean signed = isAddSignatureOptionAvailable() ? addSignature.get() : null;

        // Эта опция не может быть доступна (так как публикация - исключительно для PAGE)
        final Boolean fromGroup = null;

        setEditingNow(true);
        appendDisposable(wallInteractor
                .post(accountId, post.getOwnerId(), null, fromGroup, body, getAttachmentTokens(), null,
                        signed, publishDate, null, null, null, post.getVkid(), null, null, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(post -> onEditResponse(), throwable -> onEditError(getCauseIfRuntime(throwable))));
    }

    private void onEditError(Throwable throwable) {
        setEditingNow(false);
        throwable.printStackTrace();

        showError(getView(), throwable);
    }

    private void onEditResponse() {
        setEditingNow(false);
        this.canExit = true;

        if (isGuiReady()) {
            getView().closeAsSuccess();
        }
    }

    private List<AbsModel> getAttachmentTokens() {
        List<AbsModel> result = new ArrayList<>();

        for (AttachmenEntry entry : super.getData()) {
            if (entry.getAttachment() instanceof Post) {
                continue;
            }

            result.add(entry.getAttachment());
        }

        return result;
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        super.manuallyRemoveElement(index);

        if (attachment.getAttachment() instanceof Poll) {
            // because only 1 poll is supported
            resolveSupportButtons();
        }
    }

    @Override
    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), uploadDestination, photos, size, false);
        UploadUtils.upload(getApplicationContext(), intents);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_POST, post);
    }

    @OnGuiCreated
    private void resolveSupportButtons() {
        if (isGuiReady()) {
            if (post.hasCopyHierarchy()) {
                getView().setSupportedButtons(false, false, false, false, false, false);
            } else {
                getView().setSupportedButtons(true, true, true, true, isPollSupported(), supportTimer());
            }
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

    private boolean supportTimer() {
        if (getOwner() instanceof Community && ((Community) getOwner()).getAdminLevel() < VKApiCommunity.AdminLevel.EDITOR) {
            // если сообщество и я не одмен, то нет
            return false;
        }

        return Utils.intValueIn(post.getPostType(), VKApiPost.Type.POSTPONE, VKApiPost.Type.SUGGEST);
    }

    public final void fireReadyClick() {
        save();
    }

    public boolean onBackPressed() {
        if (canExit) {
            return true;
        }

        getView().showConfirmExitDialog();
        return false;
    }

    @Override
    protected void onTimerClick() {
        if (!supportTimer()) {
            return;
        }

        if (nonNull(getTimerValue())) {
            setTimerValue(null);
            return;
        }

        long initialDate = Unixtime.now() + 24 * 60 * 60;

        getView().showEnterTimeDialog(initialDate);
    }

    @Override
    public void fireTimerTimeSelected(long unixtime) {
        if (!validatePublishDate(unixtime)) {
            safeShowError(getView(), R.string.date_is_invalid);
            return;
        }

        super.setTimerValue(unixtime);
    }

    private static boolean validatePublishDate(long unixtime) {
        return Unixtime.now() < unixtime;
    }

    @Override
    protected void onPollCreateClick() {
        getView().openPollCreationWindow(getAccountId(), post.getOwnerId());
    }

    public void fireExitWithSavingConfirmed() {
        save();
    }

    public void fireExitWithoutSavingClick() {
        this.canExit = true;

        UploadUtils.cancelByDestination(getApplicationContext(), uploadDestination);
        getView().closeAsSuccess();
    }
}
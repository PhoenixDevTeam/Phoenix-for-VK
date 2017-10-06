package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.interfaces.IPhotosApi;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoAlbumEditor;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IEditPhotoAlbumView;
import biz.dealnote.messenger.mvp.view.base.ISteppersView;
import biz.dealnote.messenger.view.steppers.impl.CreatePhotoAlbumStepsHost;

/**
 * Created by ruslan.kolbasa on 30.11.2016.
 * phoenix
 */
public class EditPhotoAlbumPresenter extends AccountDependencyPresenter<IEditPhotoAlbumView> {

    private static final String TAG = EditPhotoAlbumPresenter.class.getCanonicalName();

    private int ownerId;
    private PhotoAlbum album;
    private PhotoAlbumEditor editor;
    private CreatePhotoAlbumStepsHost stepsHost;

    private final INetworker networker;
    private final boolean editing;

    public EditPhotoAlbumPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.networker = Injection.provideNetworkInterfaces();
        this.ownerId = ownerId;
        this.editor = PhotoAlbumEditor.create();
        this.editing = false;

        init(savedInstanceState);
    }

    public EditPhotoAlbumPresenter(int accountId, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.networker = Injection.provideNetworkInterfaces();
        this.album = album;
        this.ownerId = album.getOwnerId();
        this.editor = editor;
        this.editing = true;

        init(savedInstanceState);
    }

    private void init(@Nullable Bundle savedInstanceState) {
        stepsHost = new CreatePhotoAlbumStepsHost();
        stepsHost.setAdditionalOptionsEnable(ownerId < 0); // только в группе
        stepsHost.setPrivacySettingsEnable(ownerId > 0); // только у пользователя

        if (savedInstanceState != null) {
            stepsHost.restoreState(savedInstanceState);
        } else {
            stepsHost.setState(createInitialState());
        }
    }

    @Override
    public void onGuiCreated(@NonNull IEditPhotoAlbumView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.attachSteppersHost(stepsHost);
    }

    @NonNull
    private CreatePhotoAlbumStepsHost.PhotoAlbumState createInitialState() {
        return new CreatePhotoAlbumStepsHost.PhotoAlbumState()
                .setPrivacyComment(editor.getPrivacyComment())
                .setPrivacyView(editor.getPrivacyView())
                .setCommentsDisabled(editor.isCommentsDisabled())
                .setUploadByAdminsOnly(editor.isUploadByAdminsOnly())
                .setDescription(editor.getDescription())
                .setTitle(editor.getTitle());
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireStepNegativeButtonClick(int clickAtStep) {
        if (clickAtStep > 0) {
            stepsHost.setCurrentStep(clickAtStep - 1);
            callView(view -> view.moveSteppers(clickAtStep, clickAtStep - 1));
        } else {
            onBackOnFirstStepClick();
        }
    }

    private void onBackOnFirstStepClick() {
        getView().goBack();
    }

    public void fireStepPositiveButtonClick(int clickAtStep) {
        boolean last = clickAtStep == stepsHost.getStepsCount() - 1;
        if (!last) {
            int targetStep = clickAtStep + 1;
            stepsHost.setCurrentStep(targetStep);

            callView(view -> view.moveSteppers(clickAtStep, targetStep));
        } else {
            callView(ISteppersView::hideKeyboard);
            onFinalButtonClick();
        }
    }

    private void onFinalButtonClick() {
        int accountId = super.getAccountId();

        IPhotosApi api = networker.vkDefault(accountId).photos();

        final String title = state().getTitle();
        final String description = state().getDescription();

        //final VkApiPrivacy privacyView = state().getPrivacyView().toDto();
        //final VkApiPrivacy privacyComment = state().getPrivacyComment().toDto();

        final boolean uploadsByAdminsOnly = state().isUploadByAdminsOnly();
        final boolean commentsDisabled = state().isCommentsDisabled();

        /*if (editing) {
            appendDisposable(api.editAlbum(album.getId(), title, description, ownerId, privacyView,
                    privacyComment, uploadsByAdminsOnly, commentsDisabled)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe());
        } else {
            final Integer groupId = ownerId < 0 ? Math.abs(ownerId) : null;
            appendDisposable(api.createAlbum(title, groupId, description, privacyView, privacyComment, uploadsByAdminsOnly, commentsDisabled)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe());
        }*/
    }

    public boolean fireBackButtonClick() {
        int currentStep = stepsHost.getCurrentStep();

        if (currentStep > 0) {
            fireStepNegativeButtonClick(currentStep);
            return false;
        } else {
            return true;
        }
    }

    public void firePrivacyCommentClick() {

    }

    public void firePrivacyViewClick() {

    }

    public void fireUploadByAdminsOnlyChecked(boolean checked) {

    }

    public void fireDisableCommentsClick(boolean checked) {

    }

    private CreatePhotoAlbumStepsHost.PhotoAlbumState state() {
        return stepsHost.getState();
    }

    public void fireTitleEdit(CharSequence text) {
        state().setTitle(text.toString());
        callView(view -> view.updateStepButtonsAvailability(CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION));
    }
}
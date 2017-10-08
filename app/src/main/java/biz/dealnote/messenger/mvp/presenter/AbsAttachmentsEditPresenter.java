package biz.dealnote.messenger.mvp.presenter;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IBaseAttachmentsEditView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.FileUtil;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Predicate;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.AppPerms.hasCameraPermision;
import static biz.dealnote.messenger.util.AppPerms.hasReadStoragePermision;
import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.findInfoByPredicate;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOfMultiple;

/**
 * Created by admin on 05.12.2016.
 * phoenix
 */
public abstract class AbsAttachmentsEditPresenter<V extends IBaseAttachmentsEditView>
        extends AccountDependencyPresenter<V> {

    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_TIMER = "save_timer";
    private static final String SAVE_BODY = "save_body";
    private static final String SAVE_CURRENT_PHOTO_CAMERA_URI = "save_current_photo_camera_uri";

    private String textBody;
    private final ArrayList<AttachmenEntry> data;
    private Uri currentPhotoCameraUri;
    private Long timerValue;

    AbsAttachmentsEditPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        if (nonNull(savedInstanceState)) {
            currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI);
            textBody = savedInstanceState.getString(SAVE_BODY);
            timerValue = savedInstanceState.containsKey(SAVE_TIMER) ? savedInstanceState.getLong(SAVE_TIMER) : null;
        }

        data = new ArrayList<>();
        if(nonNull(savedInstanceState)){
            ArrayList<AttachmenEntry> savedEntries = savedInstanceState.getParcelableArrayList(SAVE_DATA);
            if(nonEmpty(savedEntries)){
                data.addAll(savedEntries);
            }
        }
    }

    Long getTimerValue() {
        return timerValue;
    }

    ArrayList<AttachmenEntry> getData() {
        return data;
    }

    @OnGuiCreated
    void resolveTimerInfoView(){
        if(isGuiReady()){
            getView().setTimerValue(timerValue);
        }
    }

    void setTimerValue(Long timerValue) {
        this.timerValue = timerValue;
        resolveTimerInfoView();
    }

    @OnGuiCreated
    void resolveTextView() {
        if (isGuiReady()) {
            getView().setTextBody(textBody);
        }
    }

    String getTextBody() {
        return textBody;
    }

    void setTextBody(String body) {
        this.textBody = body;
        resolveTextView();
    }

    ArrayList<AttachmenEntry> getNeedParcelSavingEntries(){
        return new ArrayList<>(0);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_CURRENT_PHOTO_CAMERA_URI, currentPhotoCameraUri);
        outState.putParcelableArrayList(SAVE_DATA, getNeedParcelSavingEntries());
        outState.putString(SAVE_BODY, textBody);
        if(nonNull(timerValue)){
            outState.putLong(SAVE_TIMER, timerValue);
        }
    }

    void onUploadProgressUpdate(List<IUploadQueueStore.IProgressUpdate> updates){
        Logger.d(tag(), "onUploadProgressUpdate, updates:" + updates + ", class: " + updates.getClass());

        for(IUploadQueueStore.IProgressUpdate update : updates){
            Predicate<AttachmenEntry> predicate = entry -> entry.getAttachment() instanceof UploadObject
                    && ((UploadObject) entry.getAttachment()).getId() == update.getId();

            Pair<Integer, AttachmenEntry> info = findInfoByPredicate(getData(), predicate);

            if(nonNull(info)){
                AttachmenEntry entry = info.getSecond();

                UploadObject object = (UploadObject) entry.getAttachment();
                if(object.getStatus() != UploadObject.STATUS_UPLOADING) {
                    continue;
                }

                object.setProgress(update.getProgress());

                if(isGuiReady()){
                    getView().updateProgressAtIndex(entry.getId(), update.getProgress());
                }
            }
        }
    }

    boolean onUploadObjectRemovedFromQueue(int id, @Nullable BaseUploadResponse response){
        int index = findUploadIndexById(id);
        if(index != -1){
            manuallyRemoveElement(index);
            return true;
        }

        return false;
    }

    void onUploadQueueUpdates(List<IUploadQueueStore.IQueueUpdate> updates, Predicate<UploadObject> predicate){
        //boolean hasChanges = false;
        List<IUploadQueueStore.IQueueUpdate> added = Utils.copyListWithPredicate(updates,
                update -> update.isAdding() && predicate.test(update.object()));

        if(nonEmpty(added)){
            int startSize = this.data.size();
            for(IUploadQueueStore.IQueueUpdate update : added){
                this.data.add(new AttachmenEntry(true, update.object()));
            }

            safelyNotifyItemsAdded(startSize, added.size());
        }

        for(IUploadQueueStore.IQueueUpdate update : updates){
            if(!update.isAdding()){
                onUploadObjectRemovedFromQueue(update.getId(), update.response());
            }
        }

        //if(hasChanges){
        //    safeNotifyDataSetChanged();
        //}
    }

    void safelyNotifyItemsAdded(int position, int count){
        if (isGuiReady()){
            getView().notifyItemRangeInsert(position, count);
        }
    }

    void safelyNotifyItemAdded(int position){
        safelyNotifyItemsAdded(position, 1);
    }

    List<AttachmenEntry> combine(List<AttachmenEntry> first, List<AttachmenEntry> second){
        List<AttachmenEntry> data = new ArrayList<>(safeCountOfMultiple(first, second));
        data.addAll(first);
        data.addAll(second);
        return data;
    }

    void onUploadStatusUpdate(IUploadQueueStore.IStatusUpdate update){
        Logger.d(tag(), "onUploadStatusUpdate, id: " + update.getId() + ", status: " + update.getStatus());

        int index = findUploadIndexById(update.getId());

        if(index != -1){
            UploadObject object = (UploadObject) getData().get(index).getAttachment();
            object.setStatus(update.getStatus());

            safeNotifyDataSetChanged();
        }
    }

    static List<AttachmenEntry> createFrom(List<UploadObject> objects){
        List<AttachmenEntry> data = new ArrayList<>(objects.size());
        for (UploadObject object : objects) {
            data.add(new AttachmenEntry(true, object));
        }

        return data;
    }

    static List<AttachmenEntry> createFrom(List<Pair<Integer, AbsModel>> pairs, boolean canDelete){
        List<AttachmenEntry> data = new ArrayList<>(pairs.size());
        for (Pair<Integer, AbsModel> pair : pairs) {
            data.add(new AttachmenEntry(canDelete, pair.getSecond()).setOptionalId(pair.getFirst()));
        }
        return data;
    }

    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayInitialModels(data);
    }

    public final void fireRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        if (attachment.getAttachment() instanceof UploadObject) {
            UploadObject upload = (UploadObject) attachment.getAttachment();

            UploadUtils.cancelById(getApplicationContext(), upload.getId());
            return;
        }

        onAttachmentRemoveClick(index, attachment);
    }

    void safelyNotifyItemRemoved(int position){
        if(isGuiReady()){
            getView().notifyItemRemoved(position);
        }
    }

    void onAttachmentRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        throw new UnsupportedOperationException();
    }

    void manuallyRemoveElement(int index) {
        data.remove(index);
        safelyNotifyItemRemoved(index);

        //safeNotifyDataSetChanged();
    }

    public final void fireTitleClick(int index, @NonNull AttachmenEntry attachment) {
        Logger.d(tag(), "fireTitleClick, index: " + index + ", model: " + attachment.getAttachment());
    }

    private int getMaxCountOfAttachments() {
        return 10;
    }

    private boolean canAttachMore() {
        return data.size() < getMaxCountOfAttachments();
    }

    private int getMaxFutureAttachmentCount() {
        int count = data.size() - getMaxCountOfAttachments();
        return count < 0 ? 0 : count;
    }

    public final void firePhotoFromVkChoose() {
        getView().openAddVkPhotosWindow(getMaxFutureAttachmentCount(), getAccountId(), getAccountId());
    }

    private boolean checkAbilityToAttachMore() {
        if (canAttachMore()) {
            return true;
        } else {
            safeShowError(getView(), R.string.reached_maximum_count_of_attachments);
            return false;
        }
    }

    public final void firePhotoFromLocalGalleryChoose() {
        if (!hasReadStoragePermision(getApplicationContext())) {
            getView().requestReadExternalStoragePermission();
            return;
        }

        getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
    }

    public final void firePhotoFromCameraChoose() {
        if (!hasCameraPermision(getApplicationContext())) {
            getView().requestCameraPermission();
            return;
        }

        createImageFromCamera();
    }

    private void createImageFromCamera() {
        try {
            File photoFile = FileUtil.createImageFile();
            currentPhotoCameraUri = FileUtil.getExportedUriForFile(getApplicationContext(), photoFile);

            getView().openCamera(currentPhotoCameraUri);
        } catch (IOException e) {
            safeShowError(getView(), e.getMessage());
        }
    }

    public final void firePhotoMaked() {
        getView().notifySystemAboutNewPhoto(currentPhotoCameraUri);

        LocalPhoto makedPhoto = new LocalPhoto().setFullImageUri(currentPhotoCameraUri);
        doUploadPhotos(Collections.singletonList(makedPhoto));
    }

    protected void doUploadPhotos(List<LocalPhoto> photos, int size) {
        throw new UnsupportedOperationException();
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

    public final void firePhotosFromGallerySelected(ArrayList<LocalPhoto> photos) {
        doUploadPhotos(photos);
    }

    public final void fireButtonPhotoClick() {
        if (checkAbilityToAttachMore()) {
            getView().displayChoosePhotoTypeDialog();
        }
    }

    public final void fireButtonAudioClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddAudiosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonVideoClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddVideosWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    public final void fireButtonDocClick() {
        if (checkAbilityToAttachMore()) {
            getView().openAddDocumentsWindow(getMaxFutureAttachmentCount(), getAccountId());
        }
    }

    protected void onPollCreateClick() {
        throw new UnsupportedOperationException();
    }

    protected void onTimerClick() {
        throw new UnsupportedOperationException();
    }

    public final void fireButtonPollClick() {
        onPollCreateClick();
    }

    public final void fireButtonTimerClick() {
        onTimerClick();
    }

    protected void onModelsAdded(List<? extends AbsModel> models) {
        for (AbsModel model : models) {
            data.add(new AttachmenEntry(true, model));
        }

        safeNotifyDataSetChanged();
    }

    public final void fireAudiosSelected(@NonNull ArrayList<Audio> audios) {
        onModelsAdded(audios);
    }

    public final void fireVideosSelected(@NonNull ArrayList<Video> videos) {
        onModelsAdded(videos);
    }

    public final void fireDocumentsSelected(@NonNull ArrayList<Document> documents) {
        onModelsAdded(documents);
    }

    public void fireUploadPhotoSizeSelected(@NonNull List<LocalPhoto> photos, int size) {
        doUploadPhotos(photos, size);
    }

    public final void firePollCreated(@NonNull Poll poll) {
        onModelsAdded(Collections.singletonList(poll));
    }

    protected void safeNotifyDataSetChanged() {
        if (isGuiReady()) {
            getView().notifyDataSetChanged();
        }
    }

    public final void fireTextChanged(CharSequence s) {
        textBody = isNull(s) ? null : s.toString();
    }

    public final void fireVkPhotosSelected(@NonNull ArrayList<Photo> photos) {
        onModelsAdded(photos);
    }

    public void fireCameraPermissionResolved() {
        if (hasCameraPermision(getApplicationContext())) {
            createImageFromCamera();
        }
    }

    public void fireReadStoragePermissionResolved() {
        if (hasReadStoragePermision(getApplicationContext())) {
            getView().openAddPhotoFromGalleryWindow(getMaxFutureAttachmentCount());
        }
    }

    boolean hasUploads(){
        for(AttachmenEntry entry : data){
            if(entry.getAttachment() instanceof UploadObject){
                return true;
            }
        }

        return false;
    }

    int findUploadIndexById(int id) {
        for (int i = 0; i < data.size(); i++) {
            AttachmenEntry item = data.get(i);
            if (item.getAttachment() instanceof UploadObject && ((UploadObject) item.getAttachment()).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public void fireTimerTimeSelected(long unixtime) {
        throw new UnsupportedOperationException();
    }
}

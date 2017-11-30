package biz.dealnote.messenger.fragment.attachments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.AttachmentsActivity;
import biz.dealnote.messenger.activity.PhotoAlbumsActivity;
import biz.dealnote.messenger.activity.PhotosActivity;
import biz.dealnote.messenger.adapter.AttchmentsEditorAdapter;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.AbsAttachmentsEditPresenter;
import biz.dealnote.messenger.mvp.view.IBaseAttachmentsEditView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Action;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.view.DateTimePicker;
import biz.dealnote.messenger.view.WeakRunnable;
import biz.dealnote.messenger.view.YoutubeButton;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 05.12.2016.
 * phoenix
 */
public abstract class AbsAttachmentsEditFragment<P extends AbsAttachmentsEditPresenter<V>, V extends IBaseAttachmentsEditView>
        extends BasePresenterFragment<P, V> implements IBaseAttachmentsEditView, AttchmentsEditorAdapter.Callback, BackPressCallback {

    private static final int PERMISSION_REQUEST_CAMERA = 14;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 15;

    private static final int REQUEST_PHOTO_FROM_CAMERA = 16;
    private static final int REQUEST_PHOTO_FROM_GALLERY = 17;
    private static final int REQUEST_PHOTO_FROM_VK = 18;
    private static final int REQUEST_AUDIO_SELECT = 19;
    private static final int REQUEST_VIDEO_SELECT = 20;
    private static final int REQUEST_DOCS_SELECT = 21;
    private static final int REQUEST_CREATE_POLL = 22;

    private EditText mTextBody;

    private View mTimerRoot;
    private TextView mTimerTextInfo;
    private View mTimerInfoRoot;

    private View mButtonsBar;
    private YoutubeButton mButtonPhoto;
    private YoutubeButton mButtonAudio;
    private YoutubeButton mButtonVideo;
    private YoutubeButton mButtonDoc;
    private YoutubeButton mButtonPoll;

    private Button mButtonTimer;

    private AttchmentsEditorAdapter mAdapter;

    private ViewGroup mUnderBodyContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_attachments_manager_new, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        int spancount = getResources().getInteger(R.integer.attchemtns_editor_column_count);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager manager = new GridLayoutManager(getActivity(), spancount);
        recyclerView.setLayoutManager(manager);

        View headerView = inflater.inflate(R.layout.header_attachments_manager, recyclerView, false);

        mAdapter = new AttchmentsEditorAdapter(getActivity(), Collections.emptyList(), this);
        mAdapter.addHeader(headerView);

        recyclerView.setAdapter(mAdapter);

        mUnderBodyContainer = headerView.findViewById(R.id.under_body_container);

        mTextBody = headerView.findViewById(R.id.fragment_create_post_text);
        mTextBody.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireTextChanged(s);
            }
        });

        mTimerRoot = headerView.findViewById(R.id.timer_root);
        mTimerInfoRoot = headerView.findViewById(R.id.post_schedule_info_root);
        mTimerTextInfo = headerView.findViewById(R.id.post_schedule_info);

        mButtonsBar = headerView.findViewById(R.id.buttons_bar);

        mButtonPhoto = mButtonsBar.findViewById(R.id.fragment_create_post_photo);
        mButtonAudio = mButtonsBar.findViewById(R.id.fragment_create_post_audio);
        mButtonVideo = mButtonsBar.findViewById(R.id.fragment_create_post_video);
        mButtonDoc = mButtonsBar.findViewById(R.id.fragment_create_post_doc);
        mButtonPoll = mButtonsBar.findViewById(R.id.fragment_create_post_poll);

        mButtonTimer = headerView.findViewById(R.id.button_postpone);

        mButtonPhoto.setOnClickListener(view -> getPresenter().fireButtonPhotoClick());
        mButtonAudio.setOnClickListener(view -> getPresenter().fireButtonAudioClick());
        mButtonVideo.setOnClickListener(view -> getPresenter().fireButtonVideoClick());
        mButtonDoc.setOnClickListener(view -> getPresenter().fireButtonDocClick());
        mButtonPoll.setOnClickListener(view -> getPresenter().fireButtonPollClick());

        headerView.findViewById(R.id.button_disable_postpone).setOnClickListener(v -> getPresenter().fireButtonTimerClick());
        mButtonTimer.setOnClickListener(view -> getPresenter().fireButtonTimerClick());

        mEmptyText = headerView.findViewById(R.id.empty_text);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.cleanup();
    }

    private TextView mEmptyText;

    ViewGroup getUnderBodyContainer() {
        return mUnderBodyContainer;
    }

    @Override
    public void updateProgressAtIndex(int attachmentId, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.updateEntityProgress(attachmentId, progress);
        }
    }

    @Override
    public void displayInitialModels(@NonNull List<AttachmenEntry> models) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(models);
        }

        resolveEmptyTextVisibility();
    }

    @Override
    public void setSupportedButtons(boolean photo, boolean audio, boolean video, boolean doc,
                                    boolean poll, boolean timer) {
        if (nonNull(mButtonPhoto)) {
            mButtonPhoto.setVisibility(photo ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonAudio)) {
            //mButtonAudio.setVisibility(audio ? View.VISIBLE : View.GONE);
            mButtonAudio.setVisibility(View.GONE); //audio not supported
        }

        if (nonNull(mButtonVideo)) {
            mButtonVideo.setVisibility(video ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonDoc)) {
            mButtonDoc.setVisibility(doc ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonPoll)) {
            mButtonPoll.setVisibility(poll ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mTimerRoot)) {
            mTimerRoot.setVisibility(timer ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mButtonsBar)) {
            mButtonsBar.setVisibility(photo || video || doc || poll ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setTextBody(CharSequence text) {
        if (nonNull(mTextBody)) {
            mTextBody.setText(text);
        }
    }

    @Override
    public void openAddVkPhotosWindow(int maxSelectionCount, int accountId, int ownerId) {
        Intent intent = new Intent(getActivity(), PhotoAlbumsActivity.class);
        intent.putExtra(Extra.OWNER_ID, accountId);
        intent.putExtra(Extra.ACCOUNT_ID, ownerId);
        intent.putExtra(Extra.ACTION, VKPhotosFragment.ACTION_SELECT_PHOTOS);
        startActivityForResult(intent, REQUEST_PHOTO_FROM_VK);
    }

    private void startAttachmentsActivity(int accountId, int type, int requestCode) {
        Intent intent = new Intent(getActivity(), AttachmentsActivity.class);
        intent.putExtra(Extra.TYPE, type);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void openAddAudiosWindow(int maxSelectionCount, int accountId) {
        startAttachmentsActivity(accountId, Types.AUDIO, REQUEST_AUDIO_SELECT);
    }

    @Override
    public void openAddVideosWindow(int maxSelectionCount, int accountId) {
        startAttachmentsActivity(accountId, Types.VIDEO, REQUEST_VIDEO_SELECT);
    }

    @Override
    public void openAddDocumentsWindow(int maxSelectionCount, int accountId) {
        startAttachmentsActivity(accountId, Types.DOC, REQUEST_DOCS_SELECT);
    }

    @Override
    public void openAddPhotoFromGalleryWindow(int maxSelectionCount) {
        Intent attachPhotoIntent = new Intent(getActivity(), PhotosActivity.class);
        attachPhotoIntent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, maxSelectionCount);
        startActivityForResult(attachPhotoIntent, REQUEST_PHOTO_FROM_GALLERY);
    }

    @Override
    public void onRemoveClick(int index, @NonNull AttachmenEntry attachment) {
        getPresenter().fireRemoveClick(index, attachment);
    }

    @Override
    public void onTitleClick(int index, @NonNull AttachmenEntry attachment) {
        getPresenter().fireTitleClick(index, attachment);
    }

    @Override
    public void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    @Override
    public void requestReadExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);
    }

    @Override
    public void openCamera(@NonNull Uri photoCameraUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoCameraUri);
            startActivityForResult(takePictureIntent, REQUEST_PHOTO_FROM_CAMERA);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            getPresenter().fireCameraPermissionResolved();
        }

        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            getPresenter().fireReadStoragePermissionResolved();
        }
    }

    @Override
    public void notifyItemRangeInsert(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position + mAdapter.getHeadersCount(), count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            getPresenter().firePhotoMaked();
        }

        if (requestCode == REQUEST_PHOTO_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            ArrayList<LocalPhoto> photos = data.getParcelableArrayListExtra(Extra.PHOTOS);
            AssertUtils.requireNonNull(photos);
            getPresenter().firePhotosFromGallerySelected(photos);
        }

        if (requestCode == REQUEST_AUDIO_SELECT && resultCode == Activity.RESULT_OK) {
            ArrayList<Audio> audios = data.getParcelableArrayListExtra("attachments");
            AssertUtils.requireNonNull(audios);
            getPresenter().fireAudiosSelected(audios);
        }

        if (requestCode == REQUEST_VIDEO_SELECT && resultCode == Activity.RESULT_OK) {
            ArrayList<Video> videos = data.getParcelableArrayListExtra("attachments");
            AssertUtils.requireNonNull(videos);
            getPresenter().fireVideosSelected(videos);
        }

        if (requestCode == REQUEST_DOCS_SELECT && resultCode == Activity.RESULT_OK) {
            ArrayList<Document> documents = data.getParcelableArrayListExtra("attachments");
            AssertUtils.requireNonNull(documents);
            getPresenter().fireDocumentsSelected(documents);
        }

        if (requestCode == REQUEST_CREATE_POLL && resultCode == Activity.RESULT_OK) {
            Poll poll = data.getParcelableExtra("poll");
            AssertUtils.requireNonNull(poll);
            getPresenter().firePollCreated(poll);
        }

        if (requestCode == REQUEST_PHOTO_FROM_VK && resultCode == Activity.RESULT_OK) {
            ArrayList<Photo> photos = data.getParcelableArrayListExtra("attachments");
            AssertUtils.requireNonNull(photos);
            getPresenter().fireVkPhotosSelected(photos);
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(@NonNull List<LocalPhoto> photos) {
        int[] values = {UploadObject.IMAGE_SIZE_800, UploadObject.IMAGE_SIZE_1200, UploadObject.IMAGE_SIZE_FULL};
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names,
                        (dialogInterface, index) -> getPresenter().fireUploadPhotoSizeSelected(photos, values[index]))
                .show();
    }

    @Override
    public void openPollCreationWindow(int accountId, int ownerId) {
        PlaceFactory.getCreatePollPlace(accountId, ownerId)
                .targetTo(this, REQUEST_CREATE_POLL)
                .tryOpenWith(getActivity());
    }

    @Override
    public void displayChoosePhotoTypeDialog() {
        String[] items = {getString(R.string.from_vk_albums), getString(R.string.from_local_albums), getString(R.string.from_camera)};
        new AlertDialog.Builder(getActivity()).setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    getPresenter().firePhotoFromVkChoose();
                    break;
                case 1:
                    getPresenter().firePhotoFromLocalGalleryChoose();
                    break;
                case 2:
                    getPresenter().firePhotoFromCameraChoose();
                    break;
            }
        }).show();
    }

    @Override
    public void notifySystemAboutNewPhoto(@NonNull Uri uri) {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        getActivity().sendBroadcast(scanIntent);
    }

    @Override
    public void setTimerValue(Long unixtime) {
        if (nonNull(mButtonTimer)) {
            mButtonTimer.setVisibility(isNull(unixtime) ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mTimerInfoRoot)) {
            mTimerInfoRoot.setVisibility(isNull(unixtime) ? View.GONE : View.VISIBLE);
        }

        if (nonNull(mTimerTextInfo)) {
            if (nonNull(unixtime)) {
                String formattedTime = AppTextUtils.getDateFromUnixTime(getActivity(), unixtime);
                mTimerTextInfo.setText(getString(R.string.will_be_post_at, formattedTime.toLowerCase()));
                mTimerTextInfo.setVisibility(View.VISIBLE);
            } else {
                mTimerTextInfo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(position + mAdapter.getHeadersCount());

            if(mAdapter.getRealItemCount() == 0){
                postResolveEmptyTextVisibility();
            }
        }
    }

    private void postResolveEmptyTextVisibility(){
        if(nonNull(mEmptyText)){
            final Action<AbsAttachmentsEditFragment> action = AbsAttachmentsEditFragment::resolveEmptyTextVisibility;
            mEmptyText.postDelayed(new WeakRunnable<>(this, action), 1000);
        }
    }

    private void resolveEmptyTextVisibility(){
        if(nonNull(mEmptyText)){
            mEmptyText.setVisibility(mAdapter.getRealItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(true)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void showEnterTimeDialog(long initialTimeUnixtime) {
        new DateTimePicker.Builder(getActivity())
                .setTime(initialTimeUnixtime)
                .setCallback(unixtime -> getPresenter().fireTimerTimeSelected(unixtime))
                .show();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}
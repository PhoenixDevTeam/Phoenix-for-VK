package biz.dealnote.messenger.fragment.sheet;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.AttachmentsActivity;
import biz.dealnote.messenger.activity.DualTabPhotoActivity;
import biz.dealnote.messenger.activity.VideoSelectActivity;
import biz.dealnote.messenger.adapter.AttachmentsBottomSheetAdapter;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.model.selection.LocalPhotosSelectableSource;
import biz.dealnote.messenger.model.selection.Sources;
import biz.dealnote.messenger.model.selection.VkPhotosSelectableSource;
import biz.dealnote.messenger.mvp.presenter.MessageAttachmentsPresenter;
import biz.dealnote.messenger.mvp.view.IMessageAttachmentsView;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public class MessageAttachmentsFragment extends AbsPresenterBottomSheetFragment<MessageAttachmentsPresenter,
        IMessageAttachmentsView> implements IMessageAttachmentsView, AttachmentsBottomSheetAdapter.ActionListener {

    private static final int REQUEST_ADD_VKPHOTO = 17;
    private static final int REQUEST_PERMISSION_CAMERA = 16;
    private static final int REQUEST_PHOTO_FROM_CAMERA = 15;
    private static final int REQUEST_SELECT_ATTACHMENTS = 14;

    public static MessageAttachmentsFragment newInstance(int accountId, int messageOwnerId, int messageId, ModelsBundle bundle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.MESSAGE_ID, messageId);
        args.putInt(Extra.OWNER_ID, messageOwnerId);
        args.putParcelable(Extra.BUNDLE, bundle);
        MessageAttachmentsFragment fragment = new MessageAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AttachmentsBottomSheetAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(getActivity(), R.layout.bottom_sheet_attachments, null);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        mEmptyView = view.findViewById(R.id.empty_root);

        view.findViewById(R.id.button_send).setOnClickListener(v -> {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
            getDialog().dismiss();
        });

        view.findViewById(R.id.button_hide).setOnClickListener(v -> getDialog().dismiss());
        view.findViewById(R.id.button_video).setOnClickListener(v -> getPresenter().fireButtonVideoClick());
        view.findViewById(R.id.button_doc).setOnClickListener(v -> getPresenter().fireButtonDocClick());
        view.findViewById(R.id.button_camera).setOnClickListener(v -> getPresenter().fireButtonCameraClick());

        dialog.setContentView(view);
        fireViewCreated();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = nonNull(data) ? data.getExtras() : null;
        Logger.d(tag(), "onActivityResult, extras: " + extras);

        if(requestCode == REQUEST_ADD_VKPHOTO && resultCode == Activity.RESULT_OK){
            ArrayList<Photo> vkphotos = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            ArrayList<LocalPhoto> localPhotos = data.getParcelableArrayListExtra(Extra.PHOTOS);
            getPresenter().firePhotosSelected(vkphotos, localPhotos);
        }

        if(requestCode == REQUEST_SELECT_ATTACHMENTS && resultCode == Activity.RESULT_OK){
            ArrayList<AbsModel> attachments = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            getPresenter().fireAttachmentsSelected(attachments);
        }

        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            getPresenter().firePhotoMaked();
        }
    }

    @Override
    public void savePresenterState(@NonNull MessageAttachmentsPresenter presenter, @NonNull Bundle outState) {
        presenter.saveState(outState);
    }

    @Override
    public IPresenterFactory<MessageAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int messageId = getArguments().getInt(Extra.MESSAGE_ID);
            int messageOwnerId = getArguments().getInt(Extra.OWNER_ID);
            ModelsBundle bundle = getArguments().getParcelable(Extra.BUNDLE);
            return new MessageAttachmentsPresenter(accountId, messageOwnerId, messageId, bundle, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return MessageAttachmentsFragment.class.getSimpleName();
    }

    @Override
    public void displayAttachments(List<AttachmenEntry> entries) {
        if(nonNull(mRecyclerView)){
            this.mAdapter = new AttachmentsBottomSheetAdapter(getActivity(), entries, this);
            this.mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void notifyDataAdded(int positionStart, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(positionStart + 1, count);
        }
    }

    @Override
    public void addPhoto(int accountId, int ownerId) {
        Sources sources = new Sources()
                .with(new LocalPhotosSelectableSource())
                .with(new VkPhotosSelectableSource(accountId, ownerId));

        Intent intent = DualTabPhotoActivity.createIntent(getActivity(), 10, sources);
        startActivityForResult(intent, REQUEST_ADD_VKPHOTO);
    }

    @Override
    public void notifyEntryRemoved(int index) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index + 1);
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos) {
        int[] values = {UploadObject.IMAGE_SIZE_800, UploadObject.IMAGE_SIZE_1200, UploadObject.IMAGE_SIZE_FULL};
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> getPresenter().fireUploadPhotoSizeSelected(photos, values[j]))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void changePercentageSmoothly(int dataPosition, int progress) {
        if(nonNull(mAdapter)){
            mAdapter.changeUploadProgress(dataPosition, progress, true);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemChanged(index + 1);
        }
    }

    @Override
    public void setEmptyViewVisible(boolean visible) {
        if(nonNull(mEmptyView)){
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
    }

    @Override
    public void startCamera(@NonNull Uri fileUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_PHOTO_FROM_CAMERA);
        }
    }

    @Override
    public void syncAccompanyingWithParent(ModelsBundle accompanying) {
        if(nonNull(getTargetFragment())){
            Intent data = new Intent()
                    .putExtra(Extra.BUNDLE, accompanying);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, data);
        }
    }

    @Override
    public void startAddDocumentActivity(int accountId) {
        Intent intent = AttachmentsActivity.createIntent(getActivity(), accountId, Types.DOC);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void startAddVideoActivity(int accountId, int ownerId) {
        Intent intent = VideoSelectActivity.createIntent(getActivity(), accountId, ownerId);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            getPresenter().fireCameraPermissionResolved();
        }
    }

    @Override
    public void onAddPhotoButtonClick() {
        getPresenter().fireAddPhotoButtonClick();
    }

    @Override
    public void onButtonRemoveClick(AttachmenEntry entry) {
        getPresenter().fireRemoveClick(entry);
    }

    @Override
    public void showError(String errorText) {
        if(isAdded()){
            Utils.showRedTopToast(getActivity(), errorText);
        }
    }

    @Override
    public void showError(int titleTes, Object... params) {
        if(isAdded()){
            showError(getString(titleTes, params));
        }
    }
}
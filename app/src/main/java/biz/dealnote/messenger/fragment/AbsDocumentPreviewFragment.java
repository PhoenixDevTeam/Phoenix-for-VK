package biz.dealnote.messenger.fragment;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import java.util.Collections;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.mvp.presenter.BaseDocumentPresenter;
import biz.dealnote.messenger.mvp.view.IBasicDocumentView;
import biz.dealnote.messenger.place.PlaceUtil;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public abstract class AbsDocumentPreviewFragment<P extends BaseDocumentPresenter<V>, V
        extends IBasicDocumentView> extends BasePresenterFragment<P, V> implements IBasicDocumentView {

    private static final int REQUEST_WRITE_PERMISSION = 160;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_WRITE_PERMISSION && isPresenterPrepared()){
            getPresenter().fireWritePermissionResolved();
        }
    }

    @Override
    public void requestWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    @Override
    public void shareDocument(int accountId, @NonNull Document document) {
        String[] items = new String[]{
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new AlertDialog.Builder(getActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink(getActivity(), document.generateWebLink(), document.getTitle());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(getActivity(), accountId, document);
                            break;
                        case 2:
                            PlaceUtil.goToPostCreation(getActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(document));
                            //PlaceFactory.getCreatePostPlace(accountId, accountId, EditingPostType.TEMP,
                            //        Collections.singletonList(document), null).tryOpenWith(getActivity());
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_document_title)
                .show();
    }
}
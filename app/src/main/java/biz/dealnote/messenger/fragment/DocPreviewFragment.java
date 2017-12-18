package biz.dealnote.messenger.fragment;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.domain.IDocsInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.place.PlaceUtil;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.view.CircleCounterButton;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class DocPreviewFragment extends BaseFragment implements View.OnClickListener {

    public static Bundle buildArgs(int accountId, int docId, int docOwnerId, @Nullable Document document) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.DOC_ID, docId);
        args.putInt(Extra.OWNER_ID, docOwnerId);

        if (document != null) {
            args.putParcelable(Extra.DOC, document);
        }

        return args;
    }

    public static DocPreviewFragment newInstance(Bundle arsg) {
        DocPreviewFragment fragment = new DocPreviewFragment();
        fragment.setArguments(arsg);
        return fragment;
    }

    private int accountId;
    private View rootView;
    private int ownerId;
    private int documentId;
    private Document document;

    private ImageView preview;
    private ImageView ivDocIcon;
    private TextView tvTitle;
    private TextView tvSubtitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        this.accountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.docsInteractor = InteractorFactory.createDocsInteractor();

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }

        this.ownerId = getArguments().getInt(Extra.OWNER_ID);
        this.documentId = getArguments().getInt(Extra.DOC_ID);

        if (getArguments().containsKey(Extra.DOC)) {
            this.document = getArguments().getParcelable(Extra.DOC);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_document_preview, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(rootView.findViewById(R.id.toolbar));
        preview = rootView.findViewById(R.id.fragment_document_preview);
        ivDocIcon = rootView.findViewById(R.id.no_preview_icon);
        ivDocIcon.getBackground().setColorFilter(CurrentTheme.getIconColorActive(getActivity()), PorterDuff.Mode.MULTIPLY);

        tvTitle = rootView.findViewById(R.id.fragment_document_title);
        tvSubtitle = rootView.findViewById(R.id.fragment_document_subtitle);

        CircleCounterButton deleteOrAddButton = rootView.findViewById(R.id.add_or_delete_button);

        deleteOrAddButton.setOnClickListener(this);
        rootView.findViewById(R.id.download_button).setOnClickListener(this);
        rootView.findViewById(R.id.share_button).setOnClickListener(this);

        deleteOrAddButton.setIcon(isMy() ? R.drawable.delete : R.drawable.plus);

        return rootView;
    }

    private boolean isMy() {
        return accountId == ownerId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (Objects.isNull(document) && !mLoadingNow) {
            requestVideoInfo();
        }

        resolveAllViews();
    }

    private void resolveAllViews() {
        if (!isAdded()) return;

        if (document == null) {
            rootView.findViewById(R.id.content_root).setVisibility(View.GONE);
            rootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            rootView.findViewById(R.id.progressBar).setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
            rootView.findViewById(R.id.post_loading_text).setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
            rootView.findViewById(R.id.try_again_button).setVisibility(mLoadingNow ? View.GONE : View.VISIBLE);

            return;
        }

        rootView.findViewById(R.id.content_root).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.loading_root).setVisibility(View.GONE);

        if (nonNull(document.getGraffiti())) {
            ivDocIcon.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            String graffitiUrl = document.getGraffiti().getSrc();

            if (nonEmpty(graffitiUrl)) {
                PicassoInstance.with()
                        .load(graffitiUrl)
                        .into(preview);
            }
        } else if (nonNull(document.getPhotoPreview())) {
            ivDocIcon.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            String previewUrl = document.getPhotoPreview().getUrlForSize(PhotoSize.X, true);

            if (!TextUtils.isEmpty(previewUrl)) {
                PicassoInstance.with()
                        .load(previewUrl)
                        .into(preview);
            }
        } else {
            preview.setVisibility(View.GONE);
            ivDocIcon.setVisibility(View.VISIBLE);
        }

        tvTitle.setText(document.getTitle());
        tvSubtitle.setText(AppTextUtils.getSizeString(document.getSize()));

        resolveButtons();
    }

    private void resolveButtons() {
        if (!isAdded()) {
            return;
        }

        rootView.findViewById(R.id.add_or_delete_button).setVisibility(deleted ? View.INVISIBLE : View.VISIBLE);
        rootView.findViewById(R.id.share_button).setVisibility(deleted ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean mLoadingNow;

    private void requestVideoInfo() {
        this.mLoadingNow = true;
        appendDisposable(docsInteractor.findById(accountId, ownerId, documentId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDocumentInfoReceived, this::onDocumentInfoGetError));
    }

    private void onDocumentInfoGetError(Throwable t) {
        this.mLoadingNow = false;
        // TODO: 06.10.2017
    }

    private void onDocumentInfoReceived(Document document) {
        this.mLoadingNow = false;
        this.document = document;

        getArguments().putParcelable(Extra.DOC, document);

        resolveAllViews();
        resolveActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        resolveActionBar();

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    private void resolveActionBar() {
        if (!isAdded()) return;

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.attachment_document);
            actionBar.setSubtitle(document == null ? null : document.getTitle());
        }
    }

    private static final String SAVE_DELETED = "deleted";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_DELETED, deleted);
    }

    private void restoreFromInstanceState(Bundle state) {
        this.deleted = state.getBoolean(SAVE_DELETED);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_or_delete_button:
                if (isMy()) {
                    remove();
                } else {
                    addYourSelf();
                }

                break;
            case R.id.share_button:
                share();
                break;
            case R.id.download_button:
                download();
                break;
        }
    }

    private void doRemove() {
        appendDisposable(docsInteractor.delete(accountId, documentId, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onDeleteSuccess, t -> {/*TODO*/}));
    }

    private void onDeleteSuccess() {
        if (nonNull(rootView)) {
            Snackbar.make(rootView, R.string.deleted, Snackbar.LENGTH_LONG).show();
        }

        deleted = true;
        resolveButtons();
    }

    private void remove() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.remove_confirm)
                .setMessage(R.string.doc_remove_confirm_message)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> doRemove())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean deleted = false;

    private void share() {
        String[] items = new String[]{
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new AlertDialog.Builder(getActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink(getActivity(), genLink(), document.getTitle());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(getActivity(), accountId, document);
                            break;
                        case 2:
                            postToMyWall();
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_document_title)
                .show();
    }

    private void postToMyWall() {
        List<AbsModel> models = Collections.singletonList(document);
        PlaceUtil.goToPostCreation(getActivity(), accountId, accountId, EditingPostType.TEMP, models);
    }

    private String genLink() {
        return String.format("vk.com/doc%s_%s", ownerId, documentId);
    }

    private void download() {
        if (!AppPerms.hasWriteStoragePermision(getActivity())) {
            AppPerms.requestWriteStoragePermission(getActivity());
            return;
        }

        DownloadManager.Request req = new DownloadManager.Request(Uri.parse(document.getUrl()));
        req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, document.getTitle());
        req.allowScanningByMediaScanner();
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        DownloadManager dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        dm.enqueue(req);
    }

    private void openOwnerWall() {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.goto_user).setOnMenuItemClickListener(item -> {
            openOwnerWall();
            return true;
        });
    }

    private IDocsInteractor docsInteractor;

    private void doAddYourSelf() {
        IDocsInteractor docsInteractor = InteractorFactory.createDocsInteractor();

        final String accessKey = nonNull(document) ? document.getAccessKey() : null;

        appendDisposable(docsInteractor.add(accountId, documentId, ownerId, accessKey)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(id -> onDocumentAdded(), this::onDocAddError));
    }

    private void onDocAddError(Throwable t) {
        t.printStackTrace();
    }

    private void onDocumentAdded() {
        if (nonNull(rootView)) {
            Snackbar.make(rootView, R.string.added, Snackbar.LENGTH_LONG).show();
        }

        deleted = false;
        resolveButtons();
    }

    private void addYourSelf() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.add_document_to_yourself_commit)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> doAddYourSelf())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
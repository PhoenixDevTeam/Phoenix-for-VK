package biz.dealnote.messenger.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.adapter.AttachmentsViewBinder;
import biz.dealnote.messenger.adapter.MessagesAdapter;
import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.dialog.ImageSizeAlertDialog;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria;
import biz.dealnote.messenger.fragment.sheet.MessageAttachmentsFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.FwdMessages;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.mvp.presenter.ChatPrensenter;
import biz.dealnote.messenger.mvp.view.IChatView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.InputTextDialog;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.view.InputViewController;
import biz.dealnote.messenger.view.LoadMoreFooterHelper;
import biz.dealnote.messenger.view.emoji.EmojiconTextView;
import biz.dealnote.messenger.view.emoji.StickersGridView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public class ChatFragment extends PlaceSupportPresenterFragment<ChatPrensenter, IChatView>
        implements IChatView, InputViewController.OnInputActionCallback, BackPressCallback,
        MessagesAdapter.OnMessageActionListener, InputViewController.RecordActionsCallback, AttachmentsViewBinder.VoiceActionListener, StickersGridView.OnStickerClickedListener, EmojiconTextView.OnHashTagClickListener {

    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int REQUEST_RECORD_PERMISSIONS = 15;
    private static final int REQUEST_EDIT_MESSAGE = 150;
    //private static final int REQUEST_FORWARD_MESSAGE = 151;

    public static Bundle buildArgs(int accountId, int peerId, String title, String avaUrl) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.PEER_ID, peerId);
        bundle.putString(Extra.TITLE, title);
        bundle.putString(Extra.IMAGE, avaUrl);
        return bundle;
    }

    public static ChatFragment newInstance(int accountId, int messagesOwnerId, @NonNull Peer peer) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, messagesOwnerId);
        args.putParcelable(Extra.PEER, peer);

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mHeaderView;
    private LoadMoreFooterHelper mLoadMoreFooterHelper;

    private RecyclerView mRecyclerView;
    private MessagesAdapter mAdapter;

    private InputViewController mInputViewController;
    private TextView mEmptyText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, container, false);
        root.setBackground(CurrentTheme.getChatBackground(getActivity()));

        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mEmptyText = root.findViewById(R.id.fragment_chat_empty_text);

        mRecyclerView = root.findViewById(R.id.fragment_friend_dialog_list);
        mRecyclerView.setLayoutManager(createLayoutManager());
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.getItemAnimator().setAddDuration(0);
        mRecyclerView.getItemAnimator().setMoveDuration(0);
        mRecyclerView.getItemAnimator().setRemoveDuration(0);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mLoadMoreFooterHelper = LoadMoreFooterHelper.createFrom(mHeaderView, () -> getPresenter().fireLoadUpButtonClick());

        mInputViewController = new InputViewController(getActivity(), root, true, this);

        mInputViewController.setSendOnEnter(Settings.get()
                .main()
                .isSendByEnter());

        mInputViewController.setRecordActionsCallback(this);
        mInputViewController.setOnSickerClickListener(this);
        return root;
    }

    @Override
    public void onRecordCancel() {
        getPresenter().fireRecordCancelClick();
    }

    @Override
    public void onSwithToRecordMode() {
        getPresenter().fireRecordingButtonClick();
    }

    @Override
    public void onRecordSendClick() {
        getPresenter().fireRecordSendClick();
    }

    @Override
    public void onResumePauseClick() {
        getPresenter().fireRecordResumePauseClick();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_chat, menu);
    }

    private RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);
    }

    @Override
    public void displayMessages(@NonNull List<Message> messages) {
        if (nonNull(mRecyclerView)) {
            mAdapter = new MessagesAdapter(getActivity(), messages, this);
            mAdapter.setOnMessageActionListener(this);
            mAdapter.setVoiceActionListener(this);
            mAdapter.addFooter(mHeaderView);
            mAdapter.setOnHashTagClickListener(this);

            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void notifyMessagesUpAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            int headers = mAdapter.getHeadersCount();
            mAdapter.notifyItemRangeChanged(position + headers, count); //+header if exist
        }
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {

    }

    @Override
    public void configNowVoiceMessagePlaying(int voiceId, float progress, boolean paused, boolean amin) {
        if (nonNull(mAdapter)) {
            mAdapter.configNowVoiceMessagePlaying(voiceId, progress, paused, amin);
        }
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        if (nonNull(mAdapter)) {
            mAdapter.bindVoiceHolderById(holderId, play, paused, progress, amin);
        }
    }

    @Override
    public void disableVoicePlaying() {
        if (nonNull(mAdapter)) {
            mAdapter.disableVoiceMessagePlaying();
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    public IPresenterFactory<ChatPrensenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = getArguments().getInt(Extra.ACCOUNT_ID);
            int messagesOwnerId = getArguments().getInt(Extra.OWNER_ID);
            Peer peer = getArguments().getParcelable(Extra.PEER);
            AssertUtils.requireNonNull(peer);
            return new ChatPrensenter(aid, messagesOwnerId, peer, createStartConfig(), saveInstanceState);
        };
    }

    @NonNull
    private ChatPrensenter.OutConfig createStartConfig() {
        ChatPrensenter.OutConfig config = new ChatPrensenter.OutConfig();

        config.setCloseOnSend(getActivity() instanceof SendAttachmentsActivity);

        ArrayList<Uri> inputStreams = ActivityUtils.checkLocalStreams(getActivity());
        config.setUploadFiles(safeIsEmpty(inputStreams) ? null : inputStreams);

        ModelsBundle models = getActivity().getIntent().getParcelableExtra(MainActivity.EXTRA_INPUT_ATTACHMENTS);

        if (nonNull(models)) {
            config.appendAll(models);
        }

        String initialText = ActivityUtils.checkLinks(getActivity());
        config.setInitialText(TextUtils.isEmpty(initialText) ? null : initialText);
        return config;
    }

    @Override
    public void setupLoadUpHeaderState(@LoadMoreState int state) {
        if (nonNull(mLoadMoreFooterHelper)) {
            mLoadMoreFooterHelper.switchToState(state);
        }
    }

    @Override
    public void displayDraftMessageAttachmentsCount(int count) {
        if (nonNull(mInputViewController)) {
            mInputViewController.setAttachmentsCount(count);
        }
    }

    @Override
    public void displayDraftMessageText(String text) {
        if (nonNull(mInputViewController)) {
            mInputViewController.setTextQuietly(text);
        }
    }

    @Override
    public void setupSendButton(boolean canSendNormalMessage, boolean voiceMessageSupport) {
        if (nonNull(mInputViewController)) {
            mInputViewController.setup(canSendNormalMessage, voiceMessageSupport);
        }
    }

    @Override
    public void displayToolbarTitle(String text) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(text);
        }
    }

    @Override
    public void displayToolbarSubtitle(String text) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(text);
        }
    }

    @Override
    public void setRecordModeActive(boolean active) {
        if (nonNull(mInputViewController)) {
            mInputViewController.swithModeTo(active ?
                    InputViewController.Mode.VOICE_RECORD : InputViewController.Mode.NORMAL);
        }
    }

    @Override
    public void requestRecordPermissions() {
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_RECORD_PERMISSIONS);
    }

    @Override
    public void displayRecordingDuration(long time) {
        if (nonNull(mInputViewController)) {
            mInputViewController.setRecordingDuration(time);
        }
    }

    @Override
    public void doCloseAfterSend() {
        getActivity().finish();
    }

    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback(this);

    @Override
    public void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId) {
        getPresenter().fireVoiceHolderCreated(voiceMessageId, voiceHolderId);
    }

    @Override
    public void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage) {
        getPresenter().fireVoicePlayButtonClick(voiceHolderId, voiceMessageId, voiceMessage);
    }

    @Override
    public void onStickerClick(int stickerId) {
        getPresenter().fireStickerSendClick(stickerId);
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        getPresenter().fireHashtagClick(hashTag);
    }

    private static class ActionModeCallback implements ActionMode.Callback {

        WeakReference<ChatFragment> fragmentWeakReference;

        ActionModeCallback(ChatFragment fragment) {
            this.fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ChatFragment fragment = fragmentWeakReference.get();
            if (Objects.isNull(fragment)) {
                return true;
            }

            switch (item.getItemId()) {
                case R.id.delete:
                    fragment.getPresenter().fireActionModeDeleteClick();
                    break;
                case R.id.copy:
                    fragment.getPresenter().fireActionModeCopyClick();
                    break;
                case R.id.forward:
                    fragment.getPresenter().fireForwardClick();
                    break;
            }

            mode.finish();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ChatFragment fragment = fragmentWeakReference.get();

            if (nonNull(fragment)) {
                fragment.getPresenter().fireActionModeDestroy();
                fragment.mActionMode = null;
            }
        }
    }

    @Override
    public void showActionMode(String title) {
        if (Objects.isNull(mActionMode)) {
            mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        }

        if (nonNull(mActionMode)) {
            mActionMode.setTitle(title);
            mActionMode.invalidate();
        }
    }

    @Override
    public void finishActionMode() {
        if (nonNull(mActionMode)) {
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_MESSAGE) {
            if (nonNull(data) && data.hasExtra(Extra.BUNDLE)) {
                ModelsBundle bundle = data.getParcelableExtra(Extra.BUNDLE);
                getPresenter().fireEditMessageResult(bundle);
            }

            //String body = data.getStringExtra(Extra.BODY);
            //Attachments accompanyingState = data.getParcelableExtra("accompanying_state");

            //if (isPresenterPrepared()) {
            //    getPresenter().fireEditMessageResult(body, accompanyingState);
            //}

            if (resultCode == Activity.RESULT_OK) {
                getPresenter().fireSendClickFromAttachmens();
            }
        }

        //if (requestCode == REQUEST_FORWARD_MESSAGE) {
        //    App.getInstance().setFwdBuffer(null);
        //getPresenter().fireMessageToAnotherConversationSent();
        //}

        //if (requestCode == PhotosGridView.REQUEST_CODE_ATTACH_QUICK_ALBUM && resultCode == Activity.RESULT_OK && data != null) {
        //    int albumId = data.getIntExtra(Extra.ALBUM_ID, 0);
        //    int ownerId = data.getIntExtra(Extra.OWNER_ID, 0);
        //    AppPrefs.storeQuickPhotosAttrs(getActivity(), getAccountId(), albumId, ownerId);
        //    mInputViewController.getEmojiPopup().refreshQuickPhotos();
        //}
    }

    @Override
    public void goToMessageAttachmentsEditor(int accountId, int messageOwnerId, @NonNull UploadDestination destination,
                                             String body, @Nullable ModelsBundle attachments) {
        MessageAttachmentsFragment fragment = MessageAttachmentsFragment.newInstance(accountId, messageOwnerId, destination.getId(), attachments);
        fragment.setTargetFragment(this, REQUEST_EDIT_MESSAGE);
        fragment.show(getFragmentManager(), "message-attachments");
    }

    @Override
    public void showErrorSendDialog(@NonNull Message message) {
        String[] items = {getString(R.string.try_again), getString(R.string.delete)};

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.sending_message_failed)
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            getPresenter().fireSendAgainClick(message);
                            break;
                        case 1:
                            getPresenter().fireErrorMessageDeleteClick(message);
                            break;
                    }
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mAdapter)) {
            int headers = mAdapter.getHeadersCount();
            mAdapter.notifyItemRemoved(position + headers); // +headers count
        }
    }

    private static final int LEAVE_CHAT_VISIBLE = 1;
    private static final int CHANGE_CHAT_TITLE_VISIBLE = 2;
    private static final int CHAT_MEMBERS_VISIBLE = 3;
    private static final int ENCRYPTION_STATUS_VISIBLE = 4;
    private static final int ENCRYPTION_ENABLED = 5;
    private static final int ENCRYPTION_PLUS_ENABLED = 6;
    private static final int KEY_EXCHANGE_VISIBLE = 7;

    @Override
    public void configOptionMenu(boolean canLeaveChat, boolean canChangeTitle, boolean canShowMembers,
                                 boolean encryptionStatusVisible, boolean encryprionEnabled, boolean encryptionPlusEnabled, boolean keyExchangeVisible) {
        mOptionMenuSettings.put(LEAVE_CHAT_VISIBLE, canLeaveChat);
        mOptionMenuSettings.put(CHANGE_CHAT_TITLE_VISIBLE, canChangeTitle);
        mOptionMenuSettings.put(CHAT_MEMBERS_VISIBLE, canShowMembers);
        mOptionMenuSettings.put(ENCRYPTION_STATUS_VISIBLE, encryptionStatusVisible);
        mOptionMenuSettings.put(ENCRYPTION_ENABLED, encryprionEnabled);
        mOptionMenuSettings.put(ENCRYPTION_PLUS_ENABLED, encryptionPlusEnabled);
        mOptionMenuSettings.put(KEY_EXCHANGE_VISIBLE, keyExchangeVisible);

        getActivity().invalidateOptionsMenu();
    }

    private SparseBooleanArray mOptionMenuSettings = new SparseBooleanArray();

    @Override
    public void goToSearchMessage(int accountId, @NonNull Peer peer) {
        // TODO: 17.10.2016 peer
        //PlaceFactory.getSearchPlace(accountId, SeachTabsFragment.TAB_MESSAGES, null)
        //        .tryOpenWith(getActivity());

        MessageSeachCriteria criteria = new MessageSeachCriteria("")
                .setPeerId(peer.getId());

        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.MESSAGES, criteria).tryOpenWith(getActivity());
    }

    @Override
    public void showImageSizeSelectDialog(@NonNull List<Uri> streams) {
        new ImageSizeAlertDialog.Builder(getActivity())
                .setOnSelectedCallback(size -> getPresenter().fireImageUploadSizeSelected(streams, size))
                .setOnCancelCallback(() -> getPresenter().fireUploadCancelClick())
                .show();
    }

    @Override
    public void resetUploadImages() {
        ActivityUtils.resetInputPhotos(getActivity());
    }

    @Override
    public void resetInputAttachments() {
        getActivity().getIntent().removeExtra(MainActivity.EXTRA_INPUT_ATTACHMENTS);
        ActivityUtils.resetInputText(getActivity());
    }

    @Override
    public void notifyChatResume(int accountId, int peerId, String title, String image) {
        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onChatResume(accountId, peerId, title, image);
        }
    }

    @Override
    public void goToConversationAttachments(int accountId, int peerId) {
        String[] types = {
                VKApiAttachment.TYPE_PHOTO,
                VKApiAttachment.TYPE_VIDEO,
                VKApiAttachment.TYPE_DOC,
                VKApiAttachment.TYPE_AUDIO
        };

        String[] items = {
                getString(R.string.photos),
                getString(R.string.videos),
                getString(R.string.documents),
                getString(R.string.music)
        };

        new AlertDialog.Builder(getActivity()).setItems(items,
                (dialog, which) -> showConversationAttachments(accountId, peerId, types[which])).show();
    }

    private void showConversationAttachments(int accountId, int peerId, String type) {
        PlaceFactory.getConversationAttachmentsPlace(accountId, peerId, type).tryOpenWith(getActivity());
    }

    @Override
    public void goToChatMembers(int accountId, int chatId) {
        PlaceFactory.getChatMembersPlace(accountId, chatId)
                .tryOpenWith(getActivity());
    }

    @Override
    public void showChatTitleChangeDialog(String initialValue) {
        new InputTextDialog.Builder(getActivity())
                .setAllowEmpty(false)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(initialValue)
                .setTitleRes(R.string.change_chat_title)
                .setCallback(newValue -> getPresenter().fireChatTitleTyped(newValue))
                .show();
    }

    @Override
    public void forwardMessagesToAnotherConversation(@NonNull ArrayList<Message> messages, int accountId) {
        SendAttachmentsActivity.startForSendAttachments(getActivity(), accountId, new FwdMessages(messages));
    }

    @Override
    public void diplayForwardTypeSelectDialog(@NonNull ArrayList<Message> messages) {
        String[] items = {getString(R.string.here), getString(R.string.to_another_dialogue)};

        DialogInterface.OnClickListener listener = (dialog, which) -> {
            switch (which) {
                case 0:
                    getPresenter().fireForwardToHereClick(messages);
                    break;
                case 1:
                    getPresenter().fireForwardToAnotherClick(messages);
                    break;
            }
        };

        new AlertDialog.Builder(getActivity())
                .setItems(items, listener)
                .setCancelable(true)
                .show();
    }

    @Override
    public void setEmptyTextVisible(boolean visible) {
        if (nonNull(mEmptyText)) {
            mEmptyText.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setupRecordPauseButton(boolean available, boolean isPlaying) {
        if (nonNull(mInputViewController)) {
            mInputViewController.setupRecordPauseButton(available, isPlaying);
        }
    }

    @Override
    public void displayIniciateKeyExchangeQuestion(@KeyLocationPolicy int policy) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.key_exchange)
                .setMessage(R.string.you_dont_have_encryption_keys_stored_initiate_key_exchange)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> getPresenter().fireIniciateKeyExchangeClick(policy))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void showEncryptionKeysPolicyChooseDialog(int requestCode) {
        View view = View.inflate(getActivity(), R.layout.dialog_select_encryption_key_policy, null);
        RadioButton buttonOnDisk = view.findViewById(R.id.button_on_disk);
        RadioButton buttonInRam = view.findViewById(R.id.button_in_ram);

        buttonOnDisk.setChecked(true);

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_location_key_store)
                .setView(view)
                .setPositiveButton(R.string.button_ok, (dialogInterface, i) -> {
                    if (buttonOnDisk.isChecked()) {
                        getPresenter().fireDiskKeyStoreSelected(requestCode);
                    } else if (buttonInRam.isChecked()) {
                        getPresenter().fireRamKeyStoreSelected(requestCode);
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void showEncryptionDisclaimerDialog(int requestCode) {
        View view = View.inflate(getActivity(), R.layout.content_encryption_terms_of_use, null);
        new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.phoenix_encryption)
                .setPositiveButton(R.string.button_accept, (dialogInterface, i) -> getPresenter().fireTermsOfUseAcceptClick(requestCode))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_leave_chat).setVisible(mOptionMenuSettings.get(LEAVE_CHAT_VISIBLE, false));
        menu.findItem(R.id.action_change_chat_title).setVisible(mOptionMenuSettings.get(CHANGE_CHAT_TITLE_VISIBLE, false));
        menu.findItem(R.id.action_chat_members).setVisible(mOptionMenuSettings.get(CHAT_MEMBERS_VISIBLE, false));
        menu.findItem(R.id.action_key_exchange).setVisible(mOptionMenuSettings.get(KEY_EXCHANGE_VISIBLE, false));

        MenuItem encryptionStatusItem = menu.findItem(R.id.crypt_state);
        boolean encryptionStatusVisible = mOptionMenuSettings.get(ENCRYPTION_STATUS_VISIBLE, false);

        encryptionStatusItem.setVisible(encryptionStatusVisible);

        if (encryptionStatusVisible) {
            @AttrRes
            int attrRes = R.attr.toolbarUnlockIcon;

            if (mOptionMenuSettings.get(ENCRYPTION_ENABLED, false)) {
                if (mOptionMenuSettings.get(ENCRYPTION_PLUS_ENABLED, false)) {
                    attrRes = R.attr.toolbarLockPlusIcon;
                } else {
                    attrRes = R.attr.toolbarLockIcon;
                }
            }

            try {
                encryptionStatusItem.setIcon(CurrentTheme.getResIdFromAttribute(getActivity(), attrRes));
            } catch (Exception e) {
                //java.lang.NullPointerException: Attempt to invoke virtual method
                // 'android.content.res.Resources$Theme android.app.Activity.getTheme()' on a null object reference
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getPresenter().fireRefreshClick();
                return true;
            case R.id.action_leave_chat:
                getPresenter().fireLeaveChatClick();
                return true;
            case R.id.action_change_chat_title:
                getPresenter().fireChatTitleClick();
                return true;
            case R.id.action_chat_members:
                getPresenter().fireChatMembersClick();
                return true;
            case R.id.action_attachments_in_conversation:
                getPresenter().fireDialogAttachmentsClick();
                break;
            case R.id.messages_search:
                getPresenter().fireSearchClick();
                break;
            case R.id.crypt_state:
                getPresenter().fireEncriptionStatusClick();
                break;
            case R.id.action_key_exchange:
                getPresenter().fireKeyExchangeClick();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_PERMISSIONS && isPresenterPrepared()) {
            getPresenter().fireRecordPermissionsResolved();
        }
    }

    @Override
    public void onInputTextChanged(String s) {
        getPresenter().fireDraftMessageTextEdited(s);
    }

    @Override
    public void onSendClicked(String body) {
        getPresenter().fireSendClick();
    }

    @Override
    public void onAttachClick() {
        getPresenter().fireAttachButtonClick();
    }

    @Override
    public boolean onBackPressed() {
        return Objects.isNull(mInputViewController) || mInputViewController.onBackPressed();
    }

    public void reInit(int newAccountId, int newMessagesOwnerId, int newPeerId, String title) {
        getPresenter().reInitWithNewPeer(newAccountId, newMessagesOwnerId, newPeerId, title);
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        if (nonNull(mActionMode)) {
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        getPresenter().fireMessageRestoreClick(message);
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        getPresenter().fireMessageLongClick(message);
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInputViewController.destroyView();
        mInputViewController = null;
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.d(TAG, "finalize");
        super.finalize();
    }
}
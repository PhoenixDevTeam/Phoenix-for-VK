package biz.dealnote.messenger.mvp.presenter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.crypt.AesKeyPair;
import biz.dealnote.messenger.crypt.KeyExchangeService;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.crypt.KeyPairDoesNotExistException;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.domain.IAttachmentsRepository;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.exception.UploadNotResolvedException;
import biz.dealnote.messenger.longpoll.LongpollUtils;
import biz.dealnote.messenger.longpoll.model.AbsRealtimeAction;
import biz.dealnote.messenger.longpoll.model.MessageFlagsReset;
import biz.dealnote.messenger.longpoll.model.MessageFlagsSet;
import biz.dealnote.messenger.longpoll.model.MessagesRead;
import biz.dealnote.messenger.longpoll.model.RealtimeAction;
import biz.dealnote.messenger.longpoll.model.UserOffline;
import biz.dealnote.messenger.longpoll.model.UserOnline;
import biz.dealnote.messenger.longpoll.model.WriteText;
import biz.dealnote.messenger.media.record.AudioRecordException;
import biz.dealnote.messenger.media.record.AudioRecordWrapper;
import biz.dealnote.messenger.media.record.Recorder;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.DraftMessage;
import biz.dealnote.messenger.model.FwdMessages;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.MessageFlag;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.mvp.view.IChatView;
import biz.dealnote.messenger.realtime.Processors;
import biz.dealnote.messenger.realtime.TmpResult;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.task.TextingNotifier;
import biz.dealnote.messenger.upload.Method;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Lookup;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Unixtime;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.WeakConsumer;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.AppTextUtils.safeTrimmedIsEmpty;
import static biz.dealnote.messenger.util.CompareUtils.compareInts;
import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.getSelected;
import static biz.dealnote.messenger.util.Utils.hasFlag;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public class ChatPrensenter extends AbsMessageListPresenter<IChatView> {

    private static final String TAG = ChatPrensenter.class.getSimpleName();

    private static final int COUNT = 30;
    private static final String RECORD_EXT_MP3 = "mp3";

    private static final String SAVE_PEER = "save_peer";
    private static final String SAVE_DRAFT_MESSAGE_TEXT = "save_draft_message_text";
    private static final String SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT = "save_draft_message_attachments_count";
    private static final String SAVE_DRAFT_MESSAGE_ID = "save_draft_message_id";
    private static final String SAVE_CONFIG = "save_config";

    private static final int REQUEST_CODE_ENABLE_ENCRYPTION = 1;
    private static final int REQUEST_CODE_KEY_EXCHANGE = 2;

    private static final Comparator<Message> MESSAGES_COMPARATOR = (rhs, lhs) -> {
        // соблюдаем сортировку как при запросе в бд
        if (lhs.getStatus() == rhs.getStatus()) {
            return compareInts(lhs.getId(), rhs.getId());
        }

        return compareInts(lhs.getStatus(), rhs.getStatus());
    };

    private Peer mPeer;
    private String mSubtitle;
    private AudioRecordWrapper mAudioRecordWrapper;
    private boolean mEndOfContent;
    private OutConfig mOutConfig;
    private String mDraftMessageText;
    private Integer mDraftMessageId;
    private TextingNotifier mTextingNotifier;
    private ToolbarSubtitleHandler mToolbarSubtitleHandler = new ToolbarSubtitleHandler(this);
    private int mDraftMessageDbAttachmentsCount;

    private Lookup mRecordingLookup;

    /**
     * Владелец "сообщений". Может быть больше или меньше ноля (сообщения группы)
     */
    private final int messagesOwnerId;

    private final IMessagesInteractor messagesInteractor;

    public ChatPrensenter(int accountId, int messagesOwnerId, @NonNull Peer initialPeer, @NonNull OutConfig config, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.messagesInteractor = InteractorFactory.createMessagesInteractor();
        this.messagesOwnerId = messagesOwnerId;

        mAudioRecordWrapper = new AudioRecordWrapper.Builder(App.getInstance())
                .setFileExt(RECORD_EXT_MP3)
                .build();

        if (isNull(savedInstanceState)) {
            mPeer = initialPeer;
            mOutConfig = config;

            if (nonEmpty(config.getInitialText())) {
                mDraftMessageText = config.getInitialText();
            }
        } else {
            restoreFromInstanceState(savedInstanceState);
        }

        loadAllCachedData();
        requestAtStart();

        if (isNull(savedInstanceState)) {
            tryToRestoreDraftMessage(nonEmpty(this.mDraftMessageText));
        }

        resolveAccountHotSwapSupport();
        mTextingNotifier = new TextingNotifier(messagesOwnerId);

        Predicate<IAttachmentsRepository.IBaseEvent> predicate = event -> nonNull(mDraftMessageId)
                && event.getAccountId() == messagesOwnerId
                && event.getAttachToId() == mDraftMessageId;

        IAttachmentsRepository attachmentsRepository = Injection.provideAttachmentsRepository();

        appendDisposable(attachmentsRepository
                .observeAdding()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onRepositoryAttachmentsAdded(event.getAttachments().size())));

        appendDisposable(attachmentsRepository
                .observeRemoving()
                .filter(predicate)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(event -> onRepositoryAttachmentsRemoved()));

        appendDisposable(Stores.getInstance()
                .messages()
                .observeMessageUpdates()
                .filter(update -> update.getAccountId() == messagesOwnerId && nonNull(update.getStatusUpdate()))
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(update -> {
                    Integer vkid = isNull(update.getSentUpdate()) ? null : update.getSentUpdate().getVkid();
                    onMessageStatusChange(update.getMessageId(), vkid, update.getStatusUpdate().getStatus());
                }));

        mRecordingLookup = new Lookup(1000);
        mRecordingLookup.setCallback(this::resolveRecordingTimeView);

        appendDisposable(LongpollUtils.observeUpdates(getApplicationContext())
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onRealtimeVkActionReceive));

        appendDisposable(Processors.realtimeMessages()
                .observeResults()
                .filter(result -> result.getAccountId() == messagesOwnerId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(result -> {
                    for (TmpResult.Msg msg : result.getData()) {
                        Message m = msg.getMessage();

                        if (nonNull(m) && getPeerId() == m.getPeerId()) {
                            onRealtimeMessageReceived(m);
                        }
                    }
                }));

        updateSubtitle();
    }

    @SuppressLint("SwitchIntDef")
    private void onRealtimeVkActionReceive(List<AbsRealtimeAction> actions) {
        for (AbsRealtimeAction action : actions) {
            switch (action.getAction()) {
                case RealtimeAction.KEEP_LISTENING_REQUEST:
                    if (isLongpollNeed()) {
                        LongpollUtils.register(getApplicationContext(), messagesOwnerId, getPeerId(), null, null);
                    }
                    break;
                case RealtimeAction.USER_WRITE_TEXT:
                    onUserWriteInDialog((WriteText) action);
                    break;
                case RealtimeAction.USER_IS_ONLINE:
                    onUserIsOnline((UserOnline) action);
                    break;
                case RealtimeAction.USER_IS_OFFLINE:
                    onUserIsOffline((UserOffline) action);
                    break;
                case RealtimeAction.MESSAGES_FLAGS_RESET:
                    onMessageFlagReset((MessageFlagsReset) action);
                    break;
                case RealtimeAction.MESSAGES_FLAGS_SET:
                    onMessageFlagSet((MessageFlagsSet) action);
                    break;
                case RealtimeAction.MESSAGES_READ:
                    MessagesRead read = (MessagesRead) action;
                    onLongpollMessagesRead(messagesOwnerId, read.getPeerId(), read.isOut(), read.getToMessageId());
                    break;
            }
        }
    }

    private void onUserIsOffline(UserOffline userOffline) {
        if (isChatWithUser(userOffline.getUserId())) {
            long lastSeeenUnixtime = userOffline.isByTimeout() ? Unixtime.now() - (15 * 60) : Unixtime.now();
            mSubtitle = getString(R.string.last_seen_sex_unknown, AppTextUtils.getDateFromUnixTime(lastSeeenUnixtime));
            resolveToolbarSubtitle();
        }
    }

    private void onUserIsOnline(UserOnline userOnline) {
        if (isChatWithUser(userOnline.getUserId())) {
            mSubtitle = getString(R.string.online);
            resolveToolbarSubtitle();
        }
    }

    private void onUserWriteInDialog(WriteText writeText) {
        if (getPeerId() == writeText.getPeerId() && !isGroupChat()) {
            displayUserTextingInToolbar();
        }
    }

    private void onRepositoryAttachmentsRemoved() {
        mDraftMessageDbAttachmentsCount--;
        resolveAttachmentsCounter();
        resolveSendButtonState();
    }

    private void onRepositoryAttachmentsAdded(int count) {
        mDraftMessageDbAttachmentsCount = mDraftMessageDbAttachmentsCount + count;
        resolveAttachmentsCounter();
        resolveSendButtonState();
    }

    private CompositeDisposable cacheLoadingDisposable = new CompositeDisposable();

    private void loadAllCachedData() {
        setCacheLoadingNow(true);

        cacheLoadingDisposable.add(messagesInteractor
                .getCachedPeerMessages(this.messagesOwnerId, this.getPeerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Throwable::printStackTrace));
    }

    private Integer getLastMessageIdInList() {
        return getData().size() > 0 ? getData().get(getData().size() - 1).getId() : null;
    }

    private void onCachedDataReceived(List<Message> messages) {
        setCacheLoadingNow(false);
        onAllDataLoaded(messages, false);
    }

    private void onNetDataReceived(List<Message> messages, Integer startMessageId) {
        // reset cache loading
        this.cacheLoadingDisposable.clear();
        this.cacheLoadingNow = false;
        this.mEndOfContent = isEmpty(messages);

        setNetLoadingNow(false);
        onAllDataLoaded(messages, nonNull(startMessageId));
    }

    private void onAllDataLoaded(List<Message> messages, boolean appendToList) {
        boolean all = !appendToList;

        //сохранение выделенных сообщений
        if (all) {
            List<Message> selectedList = Utils.getSelected(getData());

            for (Message selected : selectedList) {
                for (Message item : messages) {
                    if (item.getId() == selected.getId()) {
                        item.setSelected(true);
                        break;
                    }
                }
            }
        }

        int startSize = getData().size();

        if (all && !getData().isEmpty()) {
            getData().clear();
            getData().addAll(messages);

            if (isGuiReady()) {
                getView().notifyDataChanged();
            }
        } else {
            getData().addAll(messages);

            if (isGuiReady()) {
                getView().notifyMessagesUpAdded(startSize, messages.size());
            }
        }

        resolveEmptyTextVisibility();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private boolean cacheLoadingNow;

    private void setCacheLoadingNow(boolean cacheLoadingNow) {
        this.cacheLoadingNow = cacheLoadingNow;
        resolveLoadUpHeaderView();
    }

    private boolean isLoadingFromDbNow() {
        return cacheLoadingNow;
    }

    private void resetDatabaseLoading() {
        cacheLoadingDisposable.clear();
    }

    public void fireLoadUpButtonClick() {
        if (canLoadMore()) {
            requestMore();
        }
    }

    private boolean canLoadMore() {
        return !getData().isEmpty() && !isLoadingFromDbNow() && !isLoadingFromNetNow();
    }

    @OnGuiCreated
    private void resolveLoadUpHeaderView() {
        if (!isGuiReady()) return;

        boolean loading = isLoadingNow();
        getView().setupLoadUpHeaderState(loading ? LoadMoreState.LOADING : (mEndOfContent ? LoadMoreState.INVISIBLE : LoadMoreState.CAN_LOAD_MORE));
    }

    private void requestAtStart() {
        requestFromNet(null);
    }

    private boolean netLoadingNow;

    private void setNetLoadingNow(boolean netLoadingNow) {
        this.netLoadingNow = netLoadingNow;
        resolveLoadUpHeaderView();
    }

    private void requestFromNet(Integer startMessageId) {
        setNetLoadingNow(true);

        final int ownerId = this.messagesOwnerId;
        final int peerId = this.getPeerId();

        appendDisposable(messagesInteractor.getPeerMessages(ownerId, peerId, COUNT, null, startMessageId, true)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(messages -> onNetDataReceived(messages, startMessageId), this::onMessagesGetError));
    }

    private void onMessagesGetError(Throwable t) {
        setNetLoadingNow(false);
        showError(getView(), getCauseIfRuntime(t));
    }

    private void requestMore() {
        requestFromNet(getLastMessageIdInList());
    }

    private boolean isLoadingFromNetNow() {
        return netLoadingNow;
    }

    private void onMessagesRestoredSuccessfully(int id) {
        Message message = findById(id);
        if (message != null) {
            message.setDeleted(false);
            safeNotifyDataChanged();
        }
    }

    public void fireDraftMessageTextEdited(String s) {
        boolean oldState = canSendNormalMessage();
        mDraftMessageText = s;
        boolean newState = canSendNormalMessage();

        if (oldState != newState) {
            setupSendButtonState(newState, true);
        }

        Message lastMessage = safeIsEmpty(getData()) ? null : getData().get(0);

        if (nonNull(lastMessage) && !lastMessage.isOut() && !lastMessage.isRead()) {
            readAllUnreadMessages();
        }

        mTextingNotifier.notifyAboutTyping(getPeerId());
    }

    public void fireSendClick() {
        if (canSendNormalMessage()) {
            sendImpl();
        }
    }

    private void sendImpl() {
        ISettings.ISecuritySettings securitySettings = Settings.get().security();

        String trimmedBody = AppTextUtils.safeTrim(mDraftMessageText, null);
        boolean encryptionEnabled = securitySettings.isMessageEncryptionEnabled(messagesOwnerId, getPeerId());

        @KeyLocationPolicy
        int keyLocationPolicy = KeyLocationPolicy.PERSIST;
        if (encryptionEnabled) {
            keyLocationPolicy = securitySettings.getEncryptionLocationPolicy(messagesOwnerId, getPeerId());
        }

        SaveMessageBuilder builder = new SaveMessageBuilder(messagesOwnerId, mPeer.getId())
                .setBody(trimmedBody)
                .setDraftMessageId(this.mDraftMessageId)
                .setRequireEncryption(encryptionEnabled)
                .setKeyLocationPolicy(keyLocationPolicy);

        List<Message> fwds = new ArrayList<>();

        for (AbsModel model : mOutConfig.models) {
            if (model instanceof FwdMessages) {
                fwds.addAll(((FwdMessages) model).fwds);
            } else {
                builder.attach(model);
            }
        }

        builder.setForwardMessages(fwds);

        mOutConfig.models.clear();

        mOutConfig.setInitialText(null);

        mDraftMessageId = null;
        mDraftMessageText = null;
        mDraftMessageDbAttachmentsCount = 0;

        getView().resetInputAttachments();

        resolveAttachmentsCounter();
        resolveDraftMessageText();
        resolveSendButtonState();

        sendMessage(builder);

        if (mOutConfig.isCloseOnSend()) {
            getView().doCloseAfterSend();
        }
    }

    private void sendMessage(@NonNull SaveMessageBuilder builder) {
        final Context app = getApplicationContext();

        this.messagesInteractor.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .doOnSuccess(message -> startSendService(app))
                .subscribe(new WeakConsumer<>(this::onMessageSaveSuccess), this::onMessageSaveError);
    }

    private void onMessageSaveError(Throwable throwable) {
        if (!isGuiReady()) {
            return;
        }

        if (throwable instanceof KeyPairDoesNotExistException) {
            safeShowError(getView(), R.string.no_encryption_keys);
        } else if (throwable instanceof UploadNotResolvedException) {
            safeShowError(getView(), R.string.upload_not_resolved_exception_message);
        } else {
            safeShowError(getView(), throwable.getMessage());
        }
    }

    private void onMessageSaveSuccess(Message message) {
        if (!isGuiReady()) {
            return;
        }

        addMessageToList(message);
        safeNotifyDataChanged();
        //startSendService();

        //if (mOutConfig.isCloseOnSend() && isGuiReady()) {
        //    getView().doCloseAfterSend();
        //}
    }

    private void startSendService(Context context) {
        Intent intent = new Intent(context, SendService.class);
        context.startService(intent);
    }

    private void startSendService() {
        startSendService(getApplicationContext());
    }

    public void fireAttachButtonClick() {
        if (isNull(mDraftMessageId)) {
            mDraftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, getPeerId(), mDraftMessageText)
                    .blockingGet();
        }

        UploadDestination destination = UploadDestination.forMessage(mDraftMessageId);

        getView().goToMessageAttachmentsEditor(getAccountId(), messagesOwnerId, destination, mDraftMessageText, mOutConfig.models); // TODO: 15.08.2017
    }

    private boolean canSendNormalMessage() {
        boolean hasAttachments = calculateAttachmentsCount() > 0;

        boolean hasNonEmptyText = !safeTrimmedIsEmpty(mDraftMessageText);

        boolean nowUpload = nowUploadingToEditingMessage();

        Logger.d(TAG, "canSendNormalMessage, hasAttachments: " + hasAttachments +
                ", hasNonEmptyText: " + hasNonEmptyText +
                ", nowUpload: " + nowUpload);

        return hasAttachments || hasNonEmptyText || nowUpload;
    }

    @OnGuiCreated
    private void resolveEmptyTextVisibility() {
        if (isGuiReady()) {
            getView().setEmptyTextVisible(safeIsEmpty(getData()) && !isLoadingNow());
        }
    }

    private boolean isLoadingNow() {
        return isLoadingFromDbNow() || isLoadingFromNetNow();
    }

    private boolean nowUploadingToEditingMessage() {
        if (isNull(mDraftMessageId)) return false;

        UploadObject currentUploadObject = UploadUtils.getCurrent();
        return currentUploadObject != null && isUploadToThis(currentUploadObject);
    }

    @OnGuiCreated
    private void resolveSendButtonState() {
        setupSendButtonState(canSendNormalMessage(), true);
    }

    private void setupSendButtonState(boolean canSendNormalMessage, boolean voiceSupport) {
        if (isGuiReady()) {
            getView().setupSendButton(canSendNormalMessage, voiceSupport);
        }
    }

    @OnGuiCreated
    private void resolveAttachmentsCounter() {
        if (isGuiReady()) {
            getView().displayDraftMessageAttachmentsCount(calculateAttachmentsCount());
        }
    }

    @OnGuiCreated
    private void resolveDraftMessageText() {
        if (isGuiReady()) {
            getView().displayDraftMessageText(mDraftMessageText);
        }
    }

    @OnGuiCreated
    private void resolveToolbarTitle() {
        if (isGuiReady()) {
            getView().displayToolbarTitle(mPeer.getTitle());
        }
    }

    public void fireRecordCancelClick() {
        mAudioRecordWrapper.stopRecording();
        onRecordingStateChanged();
        resolveRecordPauseButton();
    }

    private void onRecordingStateChanged() {
        resolveRecordModeViews();
        syncRecordingLookupState();
    }

    public void fireRecordingButtonClick() {
        if (!hasAudioRecordPermissions()) {
            getView().requestRecordPermissions();
            return;
        }

        startRecordImpl();
    }

    private void sendRecordingMessageImpl(@NonNull File file) {
        SaveMessageBuilder builder = new SaveMessageBuilder(messagesOwnerId, getPeerId())
                .setVoiceMessageFile(file);

        this.sendMessage(builder);
    }

    public void fireRecordSendClick() {
        try {
            File file = mAudioRecordWrapper.stopRecordingAndReceiveFile();
            sendRecordingMessageImpl(file);
        } catch (AudioRecordException e) {
            e.printStackTrace();
        }

        onRecordingStateChanged();
        resolveRecordPauseButton();
    }

    public void fireRecordResumePauseClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                boolean isRecorderPaused = mAudioRecordWrapper.getRecorderStatus() == Recorder.Status.PAUSED;
                if (!isRecorderPaused) {
                    mAudioRecordWrapper.pause();
                } else {
                    mAudioRecordWrapper.doRecord();
                }

                resolveRecordPauseButton();
            } catch (AudioRecordException e) {
                e.printStackTrace();
            }
        } else {
            safeShowLongToast(getView(), R.string.pause_is_not_supported);
        }
    }

    @OnGuiCreated
    private void resolveRecordModeViews() {
        if (isGuiReady()) {
            getView().setRecordModeActive(isRecordingNow());
        }
    }

    @OnGuiCreated
    private void resolveRecordPauseButton() {
        if (isGuiReady()) {
            boolean paused = mAudioRecordWrapper.getRecorderStatus() == Recorder.Status.PAUSED;
            boolean available = mAudioRecordWrapper.isPauseSupported();
            getView().setupRecordPauseButton(available, !paused);
        }
    }

    public void fireRecordPermissionsResolved() {
        if (hasAudioRecordPermissions()) {
            startRecordImpl();
        }
    }

    private void startRecordImpl() {
        try {
            mAudioRecordWrapper.doRecord();
        } catch (AudioRecordException e) {
            e.printStackTrace();
        }

        onRecordingStateChanged();
        resolveRecordingTimeView();
    }

    private boolean hasAudioRecordPermissions() {
        final Context app = getApplicationContext();

        int recordPermission = ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO);
        int writePermission = ContextCompat.checkSelfPermission(app, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return recordPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isRecordingNow() {
        int status = mAudioRecordWrapper.getRecorderStatus();
        return status == Recorder.Status.PAUSED || status == Recorder.Status.RECORDING_NOW;
    }

    private void syncRecordingLookupState() {
        if (isRecordingNow()) {
            mRecordingLookup.start();
        } else {
            mRecordingLookup.stop();
        }
    }

    @OnGuiCreated
    private void resolveRecordingTimeView() {
        if (isGuiReady() && isRecordingNow()) {
            getView().displayRecordingDuration(mAudioRecordWrapper.getCurrentRecordDuration());
        }
    }

    private void addMessageToList(@NonNull Message message) {
        Utils.addElementToList(message, getData(), MESSAGES_COMPARATOR);
    }

    private void onMessageStatusChange(int mdbid, @Nullable Integer vkid, @MessageStatus int status) {
        boolean sent = nonNull(vkid);

        int targetIndex = indexOf(mdbid);

        if (sent) {
            boolean alreadyExist = indexOf(vkid) != -1;

            if (alreadyExist) {
                if (targetIndex != -1) {
                    getData().remove(targetIndex);
                }
            } else {
                if (targetIndex != -1) {
                    Message message = getData().get(targetIndex);
                    message.setStatus(status);
                    message.setId(vkid);

                    getData().remove(targetIndex);
                    addMessageToList(message);
                }
            }
        } else {
            if (targetIndex != -1) {
                Message message = getData().get(targetIndex);
                message.setStatus(status);
            }
        }

        safeNotifyDataChanged();
    }

    private void onRealtimeMessageReceived(@NonNull Message message) {
        if (message.getPeerId() != mPeer.getId() || messagesOwnerId != message.getAccountId())
            return;

        if (message.isChatTitleUpdate()) {
            mPeer.setTitle(message.getActionText());
            resolveToolbarTitle();
        }

        int index = indexOf(message.getId());
        if (index != -1) {
            Message exist = getData().get(index);
            if (exist.isRead()) {
                message.setRead(true);
            }

            getData().remove(index);
        }

        if (message.isOut() && message.getRandomId() > 0) {
            int indexByRandomId = findUnsentMessageIndexWithRandomId(message.getRandomId());
            if (indexByRandomId != -1) {
                getData().remove(indexByRandomId);
            }
        }

        addMessageToList(message);
        safeNotifyDataChanged();
    }

    private int findUnsentMessageIndexWithRandomId(int randomId) {
        for (int i = 0; i < getData().size(); i++) {
            Message message = getData().get(i);
            if (message.isSent()) continue;
            if (message.getId() == randomId) {
                return i;
            }
        }

        return -1;
    }

    private void onLongpollMessagesRead(int accountId, int peerId, boolean out, int localId) {
        if (messagesOwnerId != accountId || getPeerId() != peerId) return;

        // у нас сообщения в списке от нового к старому
        // ......
        // 896325
        // 896324
        // 896323
        // ......

        //onLongpollMessagesRead, out: true, localId: 1004485, last: 1001210, first: 1004485

        boolean hasChanges = false;
        for (Message message : getData()) {
            if (!message.isSent()) {
                continue;
            }

            if (message.getId() > localId) {
                continue;
            }

            if (message.isOut() == out) {
                //if (message.isRead()) {
                //    //ибо дальше уже должно быть все прочитано
                //    break;
                //}

                message.setRead(true);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            safeNotifyDataChanged();
        }
    }

    private void onMessageFlagSet(MessageFlagsSet action) {
        if (messagesOwnerId != action.getAccountId() || getPeerId() != action.getPeerId())
            return;

        if (!hasFlag(action.getMask(), MessageFlag.DELETED) && !hasFlag(action.getMask(), MessageFlag.IMPORTANT)) {
            return;
        }

        Message message = findById(action.getMessageId());
        if (isNull(message)) {
            return;
        }

        boolean changed = false;
        if (hasFlag(action.getMask(), MessageFlag.DELETED) && !message.isDeleted()) {
            message.setDeleted(true);
            changed = true;
        }

        if (hasFlag(action.getMask(), MessageFlag.IMPORTANT) && !message.isImportant()) {
            message.setImportant(true);
            changed = true;
        }

        if (changed) {
            safeNotifyDataChanged();
        }
    }

    private void onMessageFlagReset(MessageFlagsReset reset) {
        if (messagesOwnerId != reset.getAccountId() || getPeerId() != reset.getPeerId()) return;

        if (!hasFlag(reset.getMask(), MessageFlag.UNREAD)
                && !hasFlag(reset.getMask(), MessageFlag.IMPORTANT)
                && !hasFlag(reset.getMask(), MessageFlag.DELETED)) {
            //чтобы не искать сообщение в списке напрасно
            return;
        }

        Message message = findById(reset.getMessageId());
        if (isNull(message)) {
            return;
        }

        boolean changed = false;
        if (hasFlag(reset.getMask(), MessageFlag.UNREAD) && !message.isRead()) {
            message.setRead(true);
            changed = true;
        }

        if (hasFlag(reset.getMask(), MessageFlag.IMPORTANT) && message.isImportant()) {
            message.setImportant(false);
            changed = true;
        }

        if (hasFlag(reset.getMask(), MessageFlag.DELETED) && message.isDeleted()) {
            message.setDeleted(false);
            changed = true;
        }

        if (changed) {
            safeNotifyDataChanged();
        }
    }

    private boolean isChatWithUser(int userId) {
        return !isGroupChat() && Peer.toUserId(getPeerId()) == userId;
    }

    private void displayUserTextingInToolbar() {
        if (isGuiReady()) {
            String typingText = getString(R.string.user_type_message);
            getView().displayToolbarSubtitle(typingText);
            mToolbarSubtitleHandler.restoreToolbarWithDelay(3000);
        }
    }

    private boolean isGroupChat() {
        return Peer.isGroupChat(getPeerId());
    }

    private void updateSubtitle() {
        mSubtitle = null;

        int peerType = Peer.getType(getPeerId());
        switch (peerType) {
            case Peer.CHAT:
            case Peer.GROUP:
                mSubtitle = null;
                resolveToolbarSubtitle();
                break;
            case Peer.USER:
                appendDisposable(Stores.getInstance()
                        .owners()
                        .getLocalizedUserActivity(messagesOwnerId, Peer.toUserId(getPeerId()))
                        .compose(RxUtils.applyMaybeIOToMainSchedulers())
                        .subscribe(s -> {
                            mSubtitle = s;
                            resolveToolbarSubtitle();
                        }, Analytics::logUnexpectedError, this::resolveToolbarSubtitle));
                break;
        }
    }

    @OnGuiCreated
    private void resolveToolbarSubtitle() {
        if (isGuiReady()) getView().displayToolbarSubtitle(mSubtitle);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        LongpollUtils.register(getApplicationContext(), messagesOwnerId, getPeerId(), null, null);
        Processors.realtimeMessages()
                .registerNotificationsInterceptor(getPresenterId(), Pair.create(messagesOwnerId, getPeerId()));
    }

    @Override
    public void onGuiPaused() {
        super.onGuiPaused();
        LongpollUtils.unregister(getApplicationContext(), messagesOwnerId, getPeerId());
        Processors.realtimeMessages()
                .unregisterNotificationsInterceptor(getPresenterId());
    }

    private int getPeerId() {
        return mPeer.getId();
    }

    private void tryToRestoreDraftMessage(boolean ignoreBody) {
        appendDisposable(Stores.getInstance()
                .messages()
                .findDraftMessage(messagesOwnerId, getPeerId())
                .compose(RxUtils.applyMaybeIOToMainSchedulers())
                .subscribe(draft -> onDraftMessageRestored(draft, ignoreBody), Analytics::logUnexpectedError));
    }

    private int calculateAttachmentsCount() {
        int outConfigCount = 0;
        for (AbsModel model : mOutConfig.models) {
            if (model instanceof FwdMessages) {
                outConfigCount = outConfigCount + ((FwdMessages) model).fwds.size();
            } else {
                outConfigCount++;
            }
        }

        return outConfigCount + mDraftMessageDbAttachmentsCount;
    }

    private void onDraftMessageRestored(@NonNull DraftMessage message, boolean ignoreBody) {
        mDraftMessageDbAttachmentsCount = message.getAttachmentsCount();
        mDraftMessageId = message.getId();

        if (!ignoreBody) {
            mDraftMessageText = message.getBody();
        }

        resolveAttachmentsCounter();
        resolveSendButtonState();
        resolveDraftMessageText();
    }

    private void resolveAccountHotSwapSupport() {
        setSupportAccountHotSwap(!Peer.isGroupChat(getPeerId()));
    }

    @Override
    public void onDestroyed() {
        resetDatabaseLoading();
        saveDraftMessageBody();

        mToolbarSubtitleHandler.release();
        mToolbarSubtitleHandler = null;

        mRecordingLookup.stop();
        mRecordingLookup.setCallback(null);
        mRecordingLookup = null;

        mTextingNotifier.shutdown();
        mTextingNotifier = null;
        super.onDestroyed();
    }

    private void saveDraftMessageBody() {
        final int peerId = getPeerId();
        final String body = mDraftMessageText;

        Stores.getInstance().messages()
                .saveDraftMessageBody(messagesOwnerId, peerId, body)
                .subscribeOn(Schedulers.io())
                .subscribe(ignore -> {
                }, Analytics::logUnexpectedError);
    }

    @Override
    protected void onMessageClick(@NonNull Message message) {
        if (message.getStatus() == MessageStatus.ERROR) {
            getView().showErrorSendDialog(message);
        } else {
            readAllUnreadMessages();
        }
    }

    private void readAllUnreadMessages() {
        boolean has = false;

        for (Message message : getData()) {
            if (!message.isOut() && !message.isRead()) {
                message.setRead(true);
                has = true;
            }
        }

        if (has) {
            safeNotifyDataChanged();
            final int peedId = getPeerId();

            appendDisposable(messagesInteractor.markAsRead(messagesOwnerId, peedId)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> {/*ignore*/}, t -> showError(getView(), getCauseIfRuntime(t))));
        }
    }

    public void fireMessageRestoreClick(@NonNull Message message) {
        restoreMessage(message.getId());
    }

    private void restoreMessage(final int messageId) {
        appendDisposable(messagesInteractor.restoreMessage(this.messagesOwnerId, messageId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onMessagesRestoredSuccessfully(messageId), t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireEditMessageResult(ModelsBundle accompanyingModels) {
        //mDraftMessageText = body;
        mOutConfig.setModels(accompanyingModels);

        //resolveDraftMessageText();
        resolveAttachmentsCounter();
        resolveSendButtonState();
    }

    @Override
    protected void onActionModeDeleteClick() {
        super.onActionModeDeleteClick();
        deleteSelectedMessages();
    }

    /**
     * Удаление отмеченных сообщений
     * можно удалять сообщения в статусе
     * STATUS_SENT - отправляем запрос на сервис, удаление из списка произойдет через longpoll
     * STATUS_QUEUE || STATUS_ERROR - просто удаляем из БД и списка
     * STATUS_WAITING_FOR_UPLOAD - отменяем "аплоад", удаляем из БД и списка
     */
    private void deleteSelectedMessages() {
        List<Integer> sent = new ArrayList<>(0);

        boolean hasChanged = false;
        Iterator<Message> iterator = getData().iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (!message.isSelected()) {
                continue;
            }

            switch (message.getStatus()) {
                case MessageStatus.SENT:
                    sent.add(message.getId());
                    break;
                case MessageStatus.QUEUE:
                case MessageStatus.ERROR:
                case MessageStatus.SENDING:
                    deleteMessageFromDbAsync(message);
                    iterator.remove();
                    hasChanged = true;
                    break;
                case MessageStatus.WAITING_FOR_UPLOAD:
                    cancelWaitingForUploadMessage(message.getId());
                    deleteMessageFromDbAsync(message);
                    iterator.remove();
                    hasChanged = true;
                    break;
                case MessageStatus.EDITING:
                    // do nothink
                    break;
            }
        }

        if (!sent.isEmpty()) {
            appendDisposable(messagesInteractor.deleteMessages(messagesOwnerId, sent)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> {/*ignore*/}, t -> showError(getView(), getCauseIfRuntime(t))));
        }

        if (hasChanged) {
            safeNotifyDataChanged();
        }
    }

    private void cancelWaitingForUploadMessage(int messageId) {
        UploadDestination destination = UploadDestination.forMessage(messageId);
        UploadUtils.cancelByDestination(App.getInstance(), destination);
    }

    public void fireSendAgainClick(@NonNull Message message) {
        appendDisposable(Stores.getInstance()
                .messages()
                .changeMessageStatus(messagesOwnerId, message.getId(), MessageStatus.QUEUE, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::startSendService, Analytics::logUnexpectedError));
    }

    private void deleteMessageFromDbAsync(@NonNull Message message) {
        Stores.getInstance()
                .messages()
                .deleteMessage(messagesOwnerId, message.getId())
                .subscribeOn(Schedulers.io())
                .subscribe(ignore -> {
                }, Analytics::logUnexpectedError);
    }

    public void fireErrorMessageDeleteClick(@NonNull Message message) {
        int index = indexOf(message.getId());
        if (index != -1) {
            getData().remove(index);
            getView().notifyItemRemoved(index);
        }

        deleteMessageFromDbAsync(message);
    }

    public void fireRefreshClick() {
        requestAtStart();
    }

    public void fireLeaveChatClick() {
        AssertUtils.assertPositive(messagesOwnerId);

        final int chatId = Peer.toChatId(getPeerId());
        final int accountId = super.getAccountId();

        appendDisposable(messagesInteractor.removeChatUser(accountId, chatId, accountId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {}, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireChatTitleClick() {
        getView().showChatTitleChangeDialog(mPeer.getTitle());
    }

    public void fireChatMembersClick() {
        AssertUtils.assertPositive(messagesOwnerId);
        getView().goToChatMembers(getAccountId(), Peer.toChatId(getPeerId()));
    }

    public void fireDialogAttachmentsClick() {
        getView().goToConversationAttachments(getAccountId(), getPeerId()); // TODO: 15.08.2017  
    }

    public void fireSearchClick() {
        getView().goToSearchMessage(getAccountId(), mPeer); // TODO: 15.08.2017  
    }

    public void fireImageUploadSizeSelected(@NonNull List<Uri> streams, int size) {
        uploadStreamsImpl(streams, size);
    }

    private void uploadStreams(@NonNull List<Uri> streams) {
        if (Utils.safeIsEmpty(streams)) return;

        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (isNull(size)) {
            getView().showImageSizeSelectDialog(streams);
        } else {
            uploadStreamsImpl(streams, size);
        }
    }

    @OnGuiCreated
    private void resolveResumePeer() {
        if (isGuiReady()) {
            getView().notifyChatResume(getAccountId(), getPeerId(), mPeer.getTitle(), mPeer.getAvaUrl()); // TODO: 15.08.2017  
        }
    }

    private void uploadStreamsImpl(@NonNull List<Uri> streams, int size) {
        mOutConfig.setUploadFiles(null);

        if (isGuiReady()) {
            getView().resetUploadImages();
        }

        if (isNull(mDraftMessageId)) {
            mDraftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, getPeerId(), mDraftMessageText)
                    .blockingGet();
        }

        UploadDestination destination = UploadDestination.forMessage(mDraftMessageId);
        List<UploadIntent> intents = new ArrayList<>(streams.size());

        for (Uri uri : streams) {
            intents.add(new UploadIntent(messagesOwnerId, destination)
                    .setAutoCommit(true)
                    .setFileUri(uri)
                    .setSize(size));
        }

        UploadUtils.upload(getApplicationContext(), intents);
    }

    public void fireUploadCancelClick() {
        mOutConfig.setUploadFiles(null);
    }

    @OnGuiCreated
    private void resolveInputImagesUploading() {
        if (!safeIsEmpty(mOutConfig.getUploadFiles())) {
            uploadStreams(mOutConfig.getUploadFiles());
        }
    }

    public void fireChatTitleTyped(String newValue) {
        final int chatId = Peer.fromChatId(getPeerId());

        appendDisposable(messagesInteractor.changeChatTitle(this.messagesOwnerId, chatId, newValue)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {/*ignore*/}, t -> showError(getView(), getCauseIfRuntime(t))));
    }

    public void fireForwardToHereClick(@NonNull ArrayList<Message> messages) {
        mOutConfig.models.append(new FwdMessages(messages));

        resolveAttachmentsCounter();
        resolveSendButtonState();
    }

    public void fireForwardToAnotherClick(@NonNull ArrayList<Message> messages) {
        getView().forwardMessagesToAnotherConversation(messages, messagesOwnerId); // TODO: 15.08.2017
    }

    @Override
    public void onActionModeForwardClick() {
        ArrayList<Message> selected = getSelected(getData());
        if (nonEmpty(selected)) {
            getView().diplayForwardTypeSelectDialog(selected);
        }
    }

    private boolean isUploadToThis(@NonNull UploadObject uploadObject) {
        return nonNull(mDraftMessageId)
                && uploadObject.getAccountId() == messagesOwnerId
                && uploadObject.getDestination().getId() == mDraftMessageId
                && uploadObject.getDestination().getMethod() == Method.PHOTO_TO_MESSAGE;
    }

    @OnGuiCreated
    private void resolveOptionMenu() {
        boolean chat = isGroupChat();
        if (isGuiReady()) {
            boolean isPlusEncryption = false;
            if (isEncryptionEnabled()) {
                isPlusEncryption = Settings.get()
                        .security()
                        .getEncryptionLocationPolicy(messagesOwnerId, getPeerId()) == KeyLocationPolicy.RAM;
            }

            getView().configOptionMenu(chat, chat, chat, isEncryptionSupport(), isEncryptionEnabled(), isPlusEncryption, isEncryptionSupport());
        }
    }

    private boolean isEncryptionSupport() {
        return Peer.isUser(getPeerId()) && getPeerId() != messagesOwnerId;
    }

    private boolean isEncryptionEnabled() {
        return Settings.get()
                .security()
                .isMessageEncryptionEnabled(messagesOwnerId, getPeerId());
    }

    public void fireEncriptionStatusClick() {
        if (!isEncryptionEnabled() && !Settings.get().security().isKeyEncryptionPolicyAccepted()) {
            getView().showEncryptionDisclaimerDialog(REQUEST_CODE_ENABLE_ENCRYPTION);
            return;
        }

        onEncryptionToggleClick();
    }

    private void onEncryptionToggleClick() {
        boolean enable = isEncryptionEnabled();
        if (enable) {
            Settings.get().security().disableMessageEncryption(messagesOwnerId, getPeerId());
            resolveOptionMenu();
        } else {
            getView().showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_ENABLE_ENCRYPTION);
        }
    }

    private void fireKeyStoreSelected(int requestCode, @KeyLocationPolicy int policy) {
        switch (requestCode) {
            case REQUEST_CODE_ENABLE_ENCRYPTION:
                onEnableEncryptionKeyStoreSelected(policy);
                break;
            case REQUEST_CODE_KEY_EXCHANGE:
                KeyExchangeService.iniciateKeyExchangeSession(getApplicationContext(), messagesOwnerId, getPeerId(), policy);
                break;
        }
    }

    public void fireDiskKeyStoreSelected(int requestCode) {
        fireKeyStoreSelected(requestCode, KeyLocationPolicy.PERSIST);
    }

    public void fireRamKeyStoreSelected(int requestCode) {
        fireKeyStoreSelected(requestCode, KeyLocationPolicy.RAM);
    }

    private void onEnableEncryptionKeyStoreSelected(@KeyLocationPolicy int policy) {
        appendDisposable(Stores.getInstance()
                .keys(policy)
                .getKeys(messagesOwnerId, getPeerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(aesKeyPairs -> fireEncriptionEnableClick(policy, aesKeyPairs), Analytics::logUnexpectedError));
    }

    private void fireEncriptionEnableClick(@KeyLocationPolicy int policy, List<AesKeyPair> pairs) {
        if (safeIsEmpty(pairs)) {
            if (isGuiReady()) {
                getView().displayIniciateKeyExchangeQuestion(policy);
            }
        } else {
            Settings.get().security().enableMessageEncryption(messagesOwnerId, getPeerId(), policy);
            resolveOptionMenu();
        }
    }

    public void fireIniciateKeyExchangeClick(@KeyLocationPolicy int policy) {
        KeyExchangeService.iniciateKeyExchangeSession(App.getInstance(), messagesOwnerId, getPeerId(), policy);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_PEER, mPeer);
        outState.putString(SAVE_DRAFT_MESSAGE_TEXT, mDraftMessageText);
        outState.putInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT, mDraftMessageDbAttachmentsCount);
        if (nonNull(mDraftMessageId)) {
            outState.putInt(SAVE_DRAFT_MESSAGE_ID, mDraftMessageId);
        }

        outState.putParcelable(SAVE_CONFIG, mOutConfig);
    }

    private void restoreFromInstanceState(@NonNull Bundle state) {
        mPeer = state.getParcelable(SAVE_PEER);
        mDraftMessageText = state.getString(SAVE_DRAFT_MESSAGE_TEXT);
        mDraftMessageDbAttachmentsCount = state.getInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT);

        if (state.containsKey(SAVE_DRAFT_MESSAGE_ID)) {
            mDraftMessageId = state.getInt(SAVE_DRAFT_MESSAGE_ID);
        }

        mOutConfig = state.getParcelable(SAVE_CONFIG);
    }

    public void fireStickerSendClick(int stickerId) {
        Sticker sticker = new Sticker(stickerId);

        SaveMessageBuilder builder = new SaveMessageBuilder(messagesOwnerId, getPeerId())
                .attach(sticker);
        sendMessage(builder);
    }

    public void fireKeyExchangeClick() {
        if (!Settings.get().security().isKeyEncryptionPolicyAccepted()) {
            getView().showEncryptionDisclaimerDialog(REQUEST_CODE_KEY_EXCHANGE);
            return;
        }

        if (isEncryptionSupport()) {
            getView().showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_KEY_EXCHANGE);
        }
    }

    @Override
    protected void afterAccountChange(int oldAccountId, int newAccountId) {
        super.afterAccountChange(oldAccountId, newAccountId);

        // TODO: 15.08.2017
        //reInitWithNewPeer(newAccountId, messagesOwnerId, getPeerId(), mPeer.getTitle());
    }

    private boolean isLongpollNeed() {
        return isGuiResumed();
    }

    public void reInitWithNewPeer(int newAccountId, int newMessagesOwnerId, int newPeerId, String title) {
        saveDraftMessageBody();

        int oldMessageOwnerId = this.messagesOwnerId;
        int oldPeerId = getPeerId();

        this.mPeer = new Peer(newPeerId).setTitle(title);

        if (isLongpollNeed()) {
            LongpollUtils.register(getApplicationContext(), newMessagesOwnerId, newPeerId, oldMessageOwnerId, oldPeerId);
            Processors.realtimeMessages()
                    .registerNotificationsInterceptor(getPresenterId(), Pair.create(messagesOwnerId, getPeerId()));
        }

        resolveAccountHotSwapSupport();
        resetDatabaseLoading();

        super.getData().clear();
        safeNotifyDataChanged();

        loadAllCachedData();
        requestAtStart();
        updateSubtitle();

        resolveToolbarTitle();
        resolveToolbarSubtitle();
        resolveOptionMenu();
        resolveResumePeer();

        mTextingNotifier = new TextingNotifier(messagesOwnerId);

        this.mDraftMessageId = null;
        this.mDraftMessageText = null;
        this.mDraftMessageDbAttachmentsCount = 0;

        boolean needToRestoreDraftMessageBody = isEmpty(mOutConfig.getInitialText());
        if (!needToRestoreDraftMessageBody) {
            this.mDraftMessageText = mOutConfig.getInitialText();
        }

        tryToRestoreDraftMessage(!needToRestoreDraftMessageBody);
    }

    public void fireTermsOfUseAcceptClick(int requestCode) {
        Settings.get().security().setKeyEncryptionPolicyAccepted(true);

        switch (requestCode) {
            case REQUEST_CODE_KEY_EXCHANGE:
                if (isEncryptionSupport()) {
                    getView().showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_KEY_EXCHANGE);
                }
                break;
            case REQUEST_CODE_ENABLE_ENCRYPTION:
                onEncryptionToggleClick();
                break;
        }
    }

    public void fireSendClickFromAttachmens() {
        fireSendClick();
    }

    private static class ToolbarSubtitleHandler extends Handler {

        static final int RESTORE_TOLLBAR = 12;

        WeakReference<ChatPrensenter> mReference;

        ToolbarSubtitleHandler(@NonNull ChatPrensenter prensenter) {
            this.mReference = new WeakReference<>(prensenter);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            ChatPrensenter prensenter = mReference.get();
            if (nonNull(prensenter) && msg.what == RESTORE_TOLLBAR) {
                prensenter.resolveToolbarSubtitle();
            }
        }

        void release() {
            removeMessages(RESTORE_TOLLBAR);
        }

        void restoreToolbarWithDelay(int delayMillis) {
            sendMessageDelayed(android.os.Message.obtain(this, RESTORE_TOLLBAR), delayMillis);
        }
    }

    public static final class OutConfig implements Parcelable {

        public static final Creator<OutConfig> CREATOR = new Creator<OutConfig>() {
            @Override
            public OutConfig createFromParcel(Parcel in) {
                return new OutConfig(in);
            }

            @Override
            public OutConfig[] newArray(int size) {
                return new OutConfig[size];
            }
        };

        private ModelsBundle models;
        private boolean closeOnSend;
        private String initialText;
        private ArrayList<Uri> uploadFiles;

        public OutConfig() {
            this.models = new ModelsBundle();
        }

        OutConfig(Parcel in) {
            this.closeOnSend = in.readByte() != 0;
            this.models = in.readParcelable(ModelsBundle.class.getClassLoader());
            this.initialText = in.readString();
            this.uploadFiles = in.createTypedArrayList(Uri.CREATOR);
        }

        public OutConfig setModels(ModelsBundle models) {
            this.models = models;
            return this;
        }

        private boolean isCloseOnSend() {
            return closeOnSend;
        }

        public OutConfig setCloseOnSend(boolean closeOnSend) {
            this.closeOnSend = closeOnSend;
            return this;
        }

        private String getInitialText() {
            return initialText;
        }

        public OutConfig setInitialText(String initialText) {
            this.initialText = initialText;
            return this;
        }

        private ArrayList<Uri> getUploadFiles() {
            return uploadFiles;
        }

        public OutConfig setUploadFiles(ArrayList<Uri> uploadFiles) {
            this.uploadFiles = uploadFiles;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (closeOnSend ? 1 : 0));
            dest.writeParcelable(models, flags);
            dest.writeString(initialText);
            dest.writeTypedList(uploadFiles);
        }

        public OutConfig appendModel(AbsModel model) {
            this.models.append(model);
            return this;
        }

        public OutConfig appendAll(Iterable<? extends AbsModel> models) {
            for (AbsModel model : models) {
                this.models.append(model);
            }
            //this.models.append(models);
            return this;
        }
    }
}
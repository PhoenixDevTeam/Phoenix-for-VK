package biz.dealnote.messenger.mvp.presenter

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import biz.dealnote.messenger.*
import biz.dealnote.messenger.crypt.AesKeyPair
import biz.dealnote.messenger.crypt.KeyExchangeService
import biz.dealnote.messenger.crypt.KeyLocationPolicy
import biz.dealnote.messenger.crypt.KeyPairDoesNotExistException
import biz.dealnote.messenger.db.Stores
import biz.dealnote.messenger.domain.IAttachmentsRepository
import biz.dealnote.messenger.domain.IMessagesInteractor
import biz.dealnote.messenger.domain.InteractorFactory
import biz.dealnote.messenger.domain.Mode
import biz.dealnote.messenger.exception.UploadNotResolvedException
import biz.dealnote.messenger.longpoll.ILongpollManager
import biz.dealnote.messenger.longpoll.LongpollInstance
import biz.dealnote.messenger.longpoll.model.*
import biz.dealnote.messenger.media.record.AudioRecordException
import biz.dealnote.messenger.media.record.AudioRecordWrapper
import biz.dealnote.messenger.media.record.Recorder
import biz.dealnote.messenger.model.*
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter
import biz.dealnote.messenger.mvp.view.IChatView
import biz.dealnote.messenger.realtime.Processors
import biz.dealnote.messenger.service.MessageSender
import biz.dealnote.messenger.settings.ISettings
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.task.TextingNotifier
import biz.dealnote.messenger.upload.IUploadManager
import biz.dealnote.messenger.upload.Method
import biz.dealnote.messenger.upload.UploadDestination
import biz.dealnote.messenger.upload.UploadIntent
import biz.dealnote.messenger.util.*
import biz.dealnote.messenger.util.Objects.isNull
import biz.dealnote.messenger.util.Objects.nonNull
import biz.dealnote.messenger.util.Optional
import biz.dealnote.messenger.util.RxUtils.*
import biz.dealnote.messenger.util.Utils.*
import biz.dealnote.mvp.reflect.OnGuiCreated
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
class ChatPrensenter(accountId: Int, private val messagesOwnerId: Int,
                     initialPeer: Peer,
                     config: ChatConfig, savedInstanceState: Bundle?) : AbsMessageListPresenter<IChatView>(accountId, savedInstanceState) {

    private var mPeer: Peer
    private var mSubtitle: String? = null
    private val mAudioRecordWrapper: AudioRecordWrapper
    private var mEndOfContent: Boolean = false
    private var mOutConfig: ChatConfig
    private var mDraftMessageText: String? = null
    private var mDraftMessageId: Int? = null
    private var mTextingNotifier: TextingNotifier
    private var mToolbarSubtitleHandler: ToolbarSubtitleHandler = ToolbarSubtitleHandler(this)
    private var mDraftMessageDbAttachmentsCount: Int = 0

    private var mRecordingLookup: Lookup

    private val messagesInteractor: IMessagesInteractor = InteractorFactory.createMessagesInteractor()
    private val longpollManager: ILongpollManager = LongpollInstance.get()
    private val uploadManager: IUploadManager = Injection.provideUploadManager()

    private val cacheLoadingDisposable = CompositeDisposable()

    private val lastMessageIdInList: Int?
        get() = if (data.size > 0) data[data.size - 1].id else null

    private var conversation: Conversation? = null

    private var isLoadingFromDbNow: Boolean = false

    private var isLoadingFromNetNow: Boolean = false

    private val isLoadingNow: Boolean
        get() = isLoadingFromDbNow || isLoadingFromNetNow

    private val isRecordingNow: Boolean
        get() {
            val status = mAudioRecordWrapper.recorderStatus
            return status == Recorder.Status.PAUSED || status == Recorder.Status.RECORDING_NOW
        }

    private val isGroupChat: Boolean
        get() = Peer.isGroupChat(peerId)

    private val peerId: Int
        get() = mPeer.id

    private val isEncryptionSupport: Boolean
        get() = Peer.isUser(peerId) && peerId != messagesOwnerId

    private val isEncryptionEnabled: Boolean
        get() = Settings.get()
                .security()
                .isMessageEncryptionEnabled(messagesOwnerId, peerId)

    init {
        mAudioRecordWrapper = AudioRecordWrapper.Builder(App.getInstance())
                .setFileExt(RECORD_EXT_MP3)
                .build()

        if (savedInstanceState == null) {
            mPeer = initialPeer
            mOutConfig = config

            if (nonEmpty(config.initialText)) {
                mDraftMessageText = config.initialText
            }
        } else {
            mPeer = savedInstanceState.getParcelable(SAVE_PEER)
            mOutConfig = savedInstanceState.getParcelable(SAVE_CONFIG)
            restoreFromInstanceState(savedInstanceState)
        }

        loadAllCachedData()
        requestAtStart()

        if (savedInstanceState == null) {
            tryToRestoreDraftMessage(nonEmpty(this.mDraftMessageText))
        }

        resolveAccountHotSwapSupport()
        mTextingNotifier = TextingNotifier(messagesOwnerId)

        val predicate = Predicate<IAttachmentsRepository.IBaseEvent> { event ->
            mDraftMessageId != null
                    && event.accountId == messagesOwnerId
                    && event.attachToId == mDraftMessageId
        }

        val attachmentsRepository = Injection.provideAttachmentsRepository()

        appendDisposable(attachmentsRepository
                .observeAdding()
                .filter(predicate)
                .toMainThread()
                .subscribe { event -> onRepositoryAttachmentsAdded(event.attachments.size) })

        appendDisposable(attachmentsRepository
                .observeRemoving()
                .filter(predicate)
                .toMainThread()
                .subscribe { _ -> onRepositoryAttachmentsRemoved() })

        appendDisposable(Stores.getInstance()
                .messages()
                .observeMessageUpdates()
                .filter { update -> update.accountId == messagesOwnerId && update.statusUpdate != null }
                .toMainThread()
                .subscribe { update ->
                    onMessageStatusChange(update.messageId, update.sentUpdate?.vkid, update.statusUpdate.status)
                })

        mRecordingLookup = Lookup(1000)
                .also {
                    it.setCallback {
                        resolveRecordingTimeView()
                    }
                }

        appendDisposable(longpollManager.observe()
                .toMainThread()
                .subscribe(Consumer { this.onRealtimeVkActionReceive(it) }, ignore()))

        appendDisposable(longpollManager.observeKeepAlive()
                .toMainThread()
                .subscribe(Consumer { onLongpollKeepAliveRequest() }, ignore()))

        appendDisposable(Processors.realtimeMessages()
                .observeResults()
                .filter { result -> result.accountId == messagesOwnerId }
                .toMainThread()
                .subscribe { result ->
                    for (msg in result.data) {
                        val m = msg.message

                        if (nonNull(m) && peerId == m.peerId) {
                            onRealtimeMessageReceived(m)
                        }
                    }
                })

        updateSubtitle()
    }

    @OnGuiCreated
    private fun resolvePinnedMessageView() {
        view?.displayPinnedMessage(if (nonNull(conversation) && nonNull(conversation!!.pinned)) Optional.wrap(conversation!!.pinned) else Optional.empty())
    }

    private fun onLongpollKeepAliveRequest() {
        checkLongpoll()
    }

    private fun onRealtimeVkActionReceive(actions: List<AbsRealtimeAction>) {
        for (action in actions) {
            when (action.action) {
                RealtimeAction.USER_WRITE_TEXT -> onUserWriteInDialog(action as WriteText)
                RealtimeAction.USER_IS_ONLINE -> onUserIsOnline(action as UserOnline)
                RealtimeAction.USER_IS_OFFLINE -> onUserIsOffline(action as UserOffline)
                RealtimeAction.MESSAGES_FLAGS_RESET -> onMessageFlagReset(action as MessageFlagsReset)
                RealtimeAction.MESSAGES_FLAGS_SET -> onMessageFlagSet(action as MessageFlagsSet)
                RealtimeAction.MESSAGES_READ -> {
                    val read = action as MessagesRead
                    onLongpollMessagesRead(messagesOwnerId, read.peerId, read.isOut, read.toMessageId)
                }
            }
        }
    }

    private fun onUserIsOffline(userOffline: UserOffline) {
        if (isChatWithUser(userOffline.userId)) {
            val lastSeeenUnixtime = if (userOffline.isByTimeout) Unixtime.now() - 15 * 60 else Unixtime.now()
            mSubtitle = getString(R.string.last_seen_sex_unknown, AppTextUtils.getDateFromUnixTime(lastSeeenUnixtime))
            resolveToolbarSubtitle()
        }
    }

    private fun onUserIsOnline(userOnline: UserOnline) {
        if (isChatWithUser(userOnline.userId)) {
            mSubtitle = getString(R.string.online)
            resolveToolbarSubtitle()
        }
    }

    private fun onUserWriteInDialog(writeText: WriteText) {
        if (peerId == writeText.peerId && !isGroupChat) {
            displayUserTextingInToolbar()
        }
    }

    private fun onRepositoryAttachmentsRemoved() {
        mDraftMessageDbAttachmentsCount--
        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    private fun onRepositoryAttachmentsAdded(count: Int) {
        mDraftMessageDbAttachmentsCount += count
        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    private fun loadAllCachedData() {
        setCacheLoadingNow(true)
        cacheLoadingDisposable.add(messagesInteractor.getConversation(messagesOwnerId, mPeer.id, Mode.ANY).singleOrError()
                .zipWith(messagesInteractor.getCachedPeerMessages(messagesOwnerId, mPeer.id), BiFunction<Conversation, List<Message>, Pair<Conversation, List<Message>>> { first, second -> Pair.create(first, second) })
                .fromIOToMain()
                .subscribe(Consumer<Pair<Conversation, List<Message>>> { this.onCachedDataReceived(it) }, ignore()))
    }

    private fun onCachedDataReceived(data: Pair<Conversation, List<Message>>) {
        setCacheLoadingNow(false)

        conversation = data.first
        resolvePinnedMessageView()
        lastReadId.`in` = conversation!!.inRead
        lastReadId.out = conversation!!.outRead
        onAllDataLoaded(data.second, false)
    }

    private fun onNetDataReceived(messages: List<Message>, startMessageId: Int?) {
        // reset cache loading
        this.cacheLoadingDisposable.clear()
        this.isLoadingFromDbNow = false
        this.mEndOfContent = isEmpty(messages)

        setNetLoadingNow(false)
        onAllDataLoaded(messages, nonNull(startMessageId))
    }

    private fun onAllDataLoaded(messages: List<Message>, appendToList: Boolean) {
        val all = !appendToList

        //сохранение выделенных сообщений
        if (all) {
            val selectedList = Utils.getSelected(data)

            for (selected in selectedList) {
                for (item in messages) {
                    if (item.id == selected.id) {
                        item.isSelected = true
                        break
                    }
                }
            }
        }

        val startSize = data.size

        if (all && !data.isEmpty()) {
            data.clear()
            data.addAll(messages)
            view?.notifyDataChanged()
        } else {
            data.addAll(messages)
            view?.notifyMessagesUpAdded(startSize, messages.size)
        }

        resolveEmptyTextVisibility()
    }

    private fun setCacheLoadingNow(cacheLoadingNow: Boolean) {
        this.isLoadingFromDbNow = cacheLoadingNow
        resolveLoadUpHeaderView()
    }

    private fun resetDatabaseLoading() {
        cacheLoadingDisposable.clear()
    }

    fun fireLoadUpButtonClick() {
        if (canLoadMore()) {
            requestMore()
        }
    }

    private fun canLoadMore(): Boolean {
        return data.isNotEmpty() && !isLoadingFromDbNow && !isLoadingFromNetNow
    }

    @OnGuiCreated
    private fun resolveLoadUpHeaderView() {
        val loading = isLoadingNow
        view?.setupLoadUpHeaderState(if (loading) LoadMoreState.LOADING else if (mEndOfContent) LoadMoreState.INVISIBLE else LoadMoreState.CAN_LOAD_MORE)
    }

    private fun requestAtStart() {
        requestFromNet(null)
    }

    private fun setNetLoadingNow(netLoadingNow: Boolean) {
        this.isLoadingFromNetNow = netLoadingNow
        resolveLoadUpHeaderView()
    }

    private fun requestFromNet(startMessageId: Int?) {
        setNetLoadingNow(true)

        val peerId = this.peerId
        appendDisposable(messagesInteractor.getPeerMessages(messagesOwnerId, peerId, COUNT, null, startMessageId, true)
                .fromIOToMain()
                .subscribe({ messages -> onNetDataReceived(messages, startMessageId) }, { this.onMessagesGetError(it) }))
    }

    private fun onMessagesGetError(t: Throwable) {
        setNetLoadingNow(false)
        showError(view, getCauseIfRuntime(t))
    }

    private fun requestMore() {
        requestFromNet(lastMessageIdInList)
    }

    private fun onMessagesRestoredSuccessfully(id: Int) {
        findById(id)?.run {
            isDeleted = false
            view?.notifyDataChanged()
        }
    }

    fun fireDraftMessageTextEdited(s: String) {
        val oldState = canSendNormalMessage()
        mDraftMessageText = s
        val newState = canSendNormalMessage()

        if (oldState != newState) {
            setupSendButtonState(newState, true)
        }

        readAllUnreadMessagesIfExists()

        mTextingNotifier.notifyAboutTyping(peerId)
    }

    fun fireSendClick() {
        if (canSendNormalMessage()) {
            sendImpl()
        }
    }

    private fun sendImpl() {
        val securitySettings = Settings.get().security()

        val trimmedBody = AppTextUtils.safeTrim(mDraftMessageText, null)
        val encryptionEnabled = securitySettings.isMessageEncryptionEnabled(messagesOwnerId, peerId)

        @KeyLocationPolicy
        var keyLocationPolicy = KeyLocationPolicy.PERSIST
        if (encryptionEnabled) {
            keyLocationPolicy = securitySettings.getEncryptionLocationPolicy(messagesOwnerId, peerId)
        }

        val builder = SaveMessageBuilder(messagesOwnerId, mPeer.id)
                .setBody(trimmedBody)
                .setDraftMessageId(this.mDraftMessageId)
                .setRequireEncryption(encryptionEnabled)
                .setKeyLocationPolicy(keyLocationPolicy)

        val fwds = ArrayList<Message>()

        for (model in mOutConfig.models) {
            if (model is FwdMessages) {
                fwds.addAll(model.fwds)
            } else {
                builder.attach(model)
            }
        }

        builder.forwardMessages = fwds

        mOutConfig.models.clear()
        mOutConfig.initialText = null

        mDraftMessageId = null
        mDraftMessageText = null
        mDraftMessageDbAttachmentsCount = 0

        view?.resetInputAttachments()

        resolveAttachmentsCounter()
        resolveDraftMessageText()
        resolveSendButtonState()

        sendMessage(builder)

        if (mOutConfig.isCloseOnSend) {
            view?.doCloseAfterSend()
        }
    }

    @SuppressLint("CheckResult")
    private fun sendMessage(builder: SaveMessageBuilder) {
        messagesInteractor.put(builder)
                .fromIOToMain()
                .doOnSuccess { _ -> startSendService() }
                .subscribe(WeakConsumer(Consumer<Message> { this.onMessageSaveSuccess(it) }), Consumer<Throwable> { this.onMessageSaveError(it) })
    }

    private fun onMessageSaveError(throwable: Throwable) {
        view?.run {
            when (throwable) {
                is KeyPairDoesNotExistException -> safeShowError(view, R.string.no_encryption_keys)
                is UploadNotResolvedException -> safeShowError(view, R.string.upload_not_resolved_exception_message)
                else -> RxSupportPresenter.safeShowError(view, throwable.message)
            }
        }
    }

    private fun onMessageSaveSuccess(message: Message) {
        addMessageToList(message)
        view?.notifyDataChanged()
    }

    private fun startSendService() {
        MessageSender.getSendService().runSendingQueue()
    }

    fun fireAttachButtonClick() {
        if (mDraftMessageId == null) {
            mDraftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, peerId, mDraftMessageText)
                    .blockingGet()
        }

        val destination = UploadDestination.forMessage(mDraftMessageId!!)
        view?.goToMessageAttachmentsEditor(accountId, messagesOwnerId, destination, mDraftMessageText, mOutConfig.models) // TODO: 15.08.2017
    }

    private fun canSendNormalMessage(): Boolean {
        return calculateAttachmentsCount() > 0 || trimmedNonEmpty(mDraftMessageText) || nowUploadingToEditingMessage()
    }

    @OnGuiCreated
    private fun resolveEmptyTextVisibility() {
        view?.setEmptyTextVisible(safeIsEmpty(data) && !isLoadingNow)
    }

    private fun nowUploadingToEditingMessage(): Boolean {
        val messageId = mDraftMessageId ?: return false

        val current = uploadManager.current
        return current.nonEmpty() && current.get().destination.compareTo(messageId, UploadDestination.WITHOUT_OWNER, Method.PHOTO_TO_MESSAGE)
    }

    @OnGuiCreated
    private fun resolveSendButtonState() {
        setupSendButtonState(canSendNormalMessage(), true)
    }

    private fun setupSendButtonState(canSendNormalMessage: Boolean, voiceSupport: Boolean) {
        view?.setupSendButton(canSendNormalMessage, voiceSupport)
    }

    @OnGuiCreated
    private fun resolveAttachmentsCounter() {
        view?.displayDraftMessageAttachmentsCount(calculateAttachmentsCount())
    }

    @OnGuiCreated
    private fun resolveDraftMessageText() {
        view?.displayDraftMessageText(mDraftMessageText)
    }

    @OnGuiCreated
    private fun resolveToolbarTitle() {
        view?.displayToolbarTitle(mPeer.title)
    }

    fun fireRecordCancelClick() {
        mAudioRecordWrapper.stopRecording()
        onRecordingStateChanged()
        resolveRecordPauseButton()
    }

    private fun onRecordingStateChanged() {
        resolveRecordModeViews()
        syncRecordingLookupState()
    }

    fun fireRecordingButtonClick() {
        if (!hasAudioRecordPermissions()) {
            view?.requestRecordPermissions()
            return
        }

        startRecordImpl()
    }

    private fun sendRecordingMessageImpl(file: File) {
        val builder = SaveMessageBuilder(messagesOwnerId, peerId).setVoiceMessageFile(file)
        this.sendMessage(builder)
    }

    fun fireRecordSendClick() {
        try {
            val file = mAudioRecordWrapper.stopRecordingAndReceiveFile()
            sendRecordingMessageImpl(file)
        } catch (e: AudioRecordException) {
            e.printStackTrace()
        }

        onRecordingStateChanged()
        resolveRecordPauseButton()
    }

    fun fireRecordResumePauseClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val isRecorderPaused = mAudioRecordWrapper.recorderStatus == Recorder.Status.PAUSED
                if (!isRecorderPaused) {
                    mAudioRecordWrapper.pause()
                } else {
                    mAudioRecordWrapper.doRecord()
                }

                resolveRecordPauseButton()
            } catch (e: AudioRecordException) {
                e.printStackTrace()
            }

        } else {
            view?.showToast(R.string.pause_is_not_supported, true)
        }
    }

    @OnGuiCreated
    private fun resolveRecordModeViews() {
        view?.setRecordModeActive(isRecordingNow)
    }

    @OnGuiCreated
    private fun resolveRecordPauseButton() {
        val paused = mAudioRecordWrapper.recorderStatus == Recorder.Status.PAUSED
        val available = mAudioRecordWrapper.isPauseSupported
        view?.setupRecordPauseButton(available, !paused)
    }

    fun fireRecordPermissionsResolved() {
        if (hasAudioRecordPermissions()) {
            startRecordImpl()
        }
    }

    private fun startRecordImpl() {
        try {
            mAudioRecordWrapper.doRecord()
        } catch (e: AudioRecordException) {
            e.printStackTrace()
        }

        onRecordingStateChanged()
        resolveRecordingTimeView()
    }

    private fun hasAudioRecordPermissions(): Boolean {
        val app = applicationContext

        val recordPermission = ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO)
        val writePermission = ContextCompat.checkSelfPermission(app, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return recordPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun syncRecordingLookupState() {
        if (isRecordingNow) {
            mRecordingLookup.start()
        } else {
            mRecordingLookup.stop()
        }
    }

    @OnGuiCreated
    private fun resolveRecordingTimeView() {
        if (isRecordingNow) {
            view?.displayRecordingDuration(mAudioRecordWrapper.currentRecordDuration)
        }
    }

    private fun addMessageToList(message: Message) {
        Utils.addElementToList(message, data, MESSAGES_COMPARATOR)
    }

    private fun onMessageStatusChange(mdbid: Int, vkid: Int?, @MessageStatus status: Int) {
        val targetIndex = indexOf(mdbid)

        if (vkid != null) {
            // message was sent
            val alreadyExist = indexOf(vkid) != -1

            if (alreadyExist) {
                if (targetIndex != -1) {
                    data.removeAt(targetIndex)
                }
            } else {
                if (targetIndex != -1) {
                    val message = data[targetIndex]
                    message.status = status
                    message.id = vkid

                    data.removeAt(targetIndex)
                    addMessageToList(message)
                }
            }
        } else {
            //message not sent
            if (targetIndex != -1) {
                val message = data[targetIndex]
                message.status = status
            }
        }

        view?.notifyDataChanged()
    }

    private fun onRealtimeMessageReceived(message: Message) {
        if (message.peerId != mPeer.id || messagesOwnerId != message.accountId) {
            return
        }

        if (message.isChatTitleUpdate) {
            mPeer.title = message.actionText
            resolveToolbarTitle()
        }

        val index = indexOf(message.id)
        if (index != -1) {
            //Message exist = getData().get(index);
            //if (exist.isRead()) {
            //    message.setRead(true);
            //}

            data.removeAt(index)
        }

        if (message.isOut && message.randomId > 0) {
            val indexByRandomId = findUnsentMessageIndexWithRandomId(message.randomId)
            if (indexByRandomId != -1) {
                data.removeAt(indexByRandomId)
            }
        }

        addMessageToList(message)
        view?.notifyDataChanged()
    }

    private fun findUnsentMessageIndexWithRandomId(randomId: Int): Int {
        for (i in 0 until data.size) {
            val message = data[i]
            if (message.isSent) continue
            if (message.id == randomId) {
                return i
            }
        }

        return -1
    }

    private fun onLongpollMessagesRead(accountId: Int, peerId: Int, out: Boolean, localId: Int) {
        if (messagesOwnerId != accountId || peerId != peerId) return

        conversation?.run {
            if (out) {
                outRead = localId
                lastReadId.out = localId
            } else {
                inRead = localId
                lastReadId.setIn(localId)
            }
        }

        view?.notifyDataChanged()

        // у нас сообщения в списке от нового к старому
        // ......
        // 896325
        // 896324
        // 896323
        // ......

        //onLongpollMessagesRead, out: true, localId: 1004485, last: 1001210, first: 1004485

        /*boolean hasChanges = false;
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
        }*/
    }

    private fun onMessageFlagSet(action: MessageFlagsSet) {
        if (messagesOwnerId != action.accountId || peerId != action.peerId) {
            return
        }

        if (!hasFlag(action.mask, MessageFlag.DELETED) && !hasFlag(action.mask, MessageFlag.IMPORTANT)) {
            return
        }

        val message = findById(action.messageId) ?: return

        var changed = false
        if (hasFlag(action.mask, MessageFlag.DELETED) && !message.isDeleted) {
            message.isDeleted = true
            changed = true
        }

        if (hasFlag(action.mask, MessageFlag.IMPORTANT) && !message.isImportant) {
            message.isImportant = true
            changed = true
        }

        if (changed) {
            view?.notifyDataChanged()
        }
    }

    private fun onMessageFlagReset(reset: MessageFlagsReset) {
        if (messagesOwnerId != reset.accountId || peerId != reset.peerId) return

        if (!hasFlag(reset.mask, MessageFlag.UNREAD)
                && !hasFlag(reset.mask, MessageFlag.IMPORTANT)
                && !hasFlag(reset.mask, MessageFlag.DELETED)) {
            //чтобы не искать сообщение в списке напрасно
            return
        }

        val message = findById(reset.messageId) ?: return

        var changed = false

        if (hasFlag(reset.mask, MessageFlag.IMPORTANT) && message.isImportant) {
            message.isImportant = false
            changed = true
        }

        if (hasFlag(reset.mask, MessageFlag.DELETED) && message.isDeleted) {
            message.isDeleted = false
            changed = true
        }

        if (changed) {
            view?.notifyDataChanged()
        }
    }

    private fun isChatWithUser(userId: Int): Boolean {
        return !isGroupChat && Peer.toUserId(peerId) == userId
    }

    private fun displayUserTextingInToolbar() {
        val typingText = getString(R.string.user_type_message)
        view?.displayToolbarSubtitle(typingText)
        mToolbarSubtitleHandler.restoreToolbarWithDelay()
    }

    private fun updateSubtitle() {
        mSubtitle = null

        val peerType = Peer.getType(peerId)

        when (peerType) {
            Peer.CHAT, Peer.GROUP -> {
                mSubtitle = null
                resolveToolbarSubtitle()
            }

            Peer.USER -> appendDisposable(Stores.getInstance()
                    .owners()
                    .getLocalizedUserActivity(messagesOwnerId, Peer.toUserId(peerId))
                    .compose(RxUtils.applyMaybeIOToMainSchedulers())
                    .subscribe({ s ->
                        mSubtitle = s
                        resolveToolbarSubtitle()
                    }, { Analytics.logUnexpectedError(it) }, { this.resolveToolbarSubtitle() }))
        }
    }

    @OnGuiCreated
    private fun resolveToolbarSubtitle() {
        view?.displayToolbarSubtitle(mSubtitle)
    }

    private fun checkLongpoll() {
        val need = isGuiResumed && accountId != ISettings.IAccountsSettings.INVALID_ID
        if (need) {
            longpollManager.keepAlive(accountId)
        }
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        checkLongpoll()
        Processors.realtimeMessages().registerNotificationsInterceptor(id, Pair.create(messagesOwnerId, peerId))
    }

    public override fun onGuiPaused() {
        super.onGuiPaused()
        checkLongpoll()
        Processors.realtimeMessages().unregisterNotificationsInterceptor(id)
    }

    private fun tryToRestoreDraftMessage(ignoreBody: Boolean) {
        appendDisposable(Stores.getInstance()
                .messages()
                .findDraftMessage(messagesOwnerId, peerId)
                .compose(RxUtils.applyMaybeIOToMainSchedulers())
                .subscribe({ draft -> onDraftMessageRestored(draft, ignoreBody) }, { Analytics.logUnexpectedError(it) }))
    }

    private fun calculateAttachmentsCount(): Int {
        var outConfigCount = 0
        for (model in mOutConfig.models) {
            if (model is FwdMessages) {
                outConfigCount += model.fwds.size
            } else {
                outConfigCount++
            }
        }

        return outConfigCount + mDraftMessageDbAttachmentsCount
    }

    private fun onDraftMessageRestored(message: DraftMessage, ignoreBody: Boolean) {
        mDraftMessageDbAttachmentsCount = message.attachmentsCount
        mDraftMessageId = message.id

        if (!ignoreBody) {
            mDraftMessageText = message.body
        }

        resolveAttachmentsCounter()
        resolveSendButtonState()
        resolveDraftMessageText()
    }

    private fun resolveAccountHotSwapSupport() {
        setSupportAccountHotSwap(!Peer.isGroupChat(peerId))
    }

    override fun onDestroyed() {
        resetDatabaseLoading()
        saveDraftMessageBody()

        mToolbarSubtitleHandler.release()

        mRecordingLookup.stop()
        mRecordingLookup.setCallback(null)

        mTextingNotifier.shutdown()
        super.onDestroyed()
    }

    private fun saveDraftMessageBody() {
        val peerId = peerId
        val body = mDraftMessageText

        Stores.getInstance()
                .messages()
                .saveDraftMessageBody(messagesOwnerId, peerId, body)
                .subscribeIOAndIgnoreResults()
    }

    override fun onMessageClick(message: Message) {
        if (message.status == MessageStatus.ERROR) {
            view?.showErrorSendDialog(message)
        } else {
            readAllUnreadMessagesIfExists()
        }
    }

    private fun readAllUnreadMessagesIfExists() {
        val last = if (nonEmpty(data)) data[0] else return

        if (nonNull(last) && !last.isOut && last.id > lastReadId.getIn()) {
            lastReadId.setIn(last.id)

            safeNotifyDataChanged()

            appendDisposable(messagesInteractor.markAsRead(messagesOwnerId, mPeer.id, last.id)
                    .fromIOToMain()
                    .subscribe(dummy(), Consumer { t -> showError(view, t) }))
        }
    }

    fun fireMessageRestoreClick(message: Message) {
        restoreMessage(message.id)
    }

    private fun restoreMessage(messageId: Int) {
        appendDisposable(messagesInteractor.restoreMessage(this.messagesOwnerId, messageId)
                .fromIOToMain()
                .subscribe({ onMessagesRestoredSuccessfully(messageId) }, { t -> showError(view, t) }))
    }

    fun fireEditMessageResult(accompanyingModels: ModelsBundle) {
        mOutConfig.models = accompanyingModels

        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    override fun onActionModeDeleteClick() {
        super.onActionModeDeleteClick()
        deleteSelectedMessages()
    }

    /**
     * Удаление отмеченных сообщений
     * можно удалять сообщения в статусе
     * STATUS_SENT - отправляем запрос на сервис, удаление из списка произойдет через longpoll
     * STATUS_QUEUE || STATUS_ERROR - просто удаляем из БД и списка
     * STATUS_WAITING_FOR_UPLOAD - отменяем "аплоад", удаляем из БД и списка
     */
    private fun deleteSelectedMessages() {
        val sent = ArrayList<Int>(0)

        var hasChanged = false
        val iterator = data.iterator()

        while (iterator.hasNext()) {
            val message = iterator.next()

            if (!message.isSelected) {
                continue
            }

            when (message.status) {
                MessageStatus.SENT -> sent.add(message.id)
                MessageStatus.QUEUE, MessageStatus.ERROR, MessageStatus.SENDING -> {
                    deleteMessageFromDbAsync(message)
                    iterator.remove()
                    hasChanged = true
                }
                MessageStatus.WAITING_FOR_UPLOAD -> {
                    cancelWaitingForUploadMessage(message.id)
                    deleteMessageFromDbAsync(message)
                    iterator.remove()
                    hasChanged = true
                }
                MessageStatus.EDITING -> {
                    // do nothink
                }
            }
        }

        if (sent.isNotEmpty()) {
            appendDisposable(messagesInteractor.deleteMessages(messagesOwnerId, sent)
                    .fromIOToMain()
                    .subscribe(dummy(), Consumer { t -> showError(view, t) }))
        }

        if (hasChanged) {
            view?.notifyDataChanged()
        }
    }

    private fun cancelWaitingForUploadMessage(messageId: Int) {
        val destination = UploadDestination.forMessage(messageId)
        uploadManager.cancelAll(messagesOwnerId, destination)
    }

    fun fireSendAgainClick(message: Message) {
        appendDisposable(Stores.getInstance()
                .messages()
                .changeMessageStatus(messagesOwnerId, message.id, MessageStatus.QUEUE, null)
                .fromIOToMain()
                .subscribe({ this.startSendService() }, { Analytics.logUnexpectedError(it) }))
    }

    private fun deleteMessageFromDbAsync(message: Message) {
        subscribeOnIOAndIgnore(Stores.getInstance()
                .messages()
                .deleteMessage(messagesOwnerId, message.id))
    }

    fun fireErrorMessageDeleteClick(message: Message) {
        val index = indexOf(message.id)
        if (index != -1) {
            data.removeAt(index)
            view?.notifyItemRemoved(index)
        }

        deleteMessageFromDbAsync(message)
    }

    fun fireRefreshClick() {
        requestAtStart()
    }

    fun fireLeaveChatClick() {
        val chatId = Peer.toChatId(peerId)
        val accountId = super.getAccountId()

        appendDisposable(messagesInteractor.removeChatUser(accountId, chatId, accountId)
                .fromIOToMain()
                .subscribe(dummy(), Consumer { t -> showError(view, t) }))
    }

    fun fireChatTitleClick() {
        view?.showChatTitleChangeDialog(mPeer.title)
    }

    fun fireChatMembersClick() {
        view?.goToChatMembers(accountId, Peer.toChatId(peerId))
    }

    fun fireDialogAttachmentsClick() {
        view?.goToConversationAttachments(accountId, peerId) // TODO: 15.08.2017
    }

    fun fireSearchClick() {
        view?.goToSearchMessage(accountId, mPeer) // TODO: 15.08.2017
    }

    fun fireImageUploadSizeSelected(streams: List<Uri>, size: Int) {
        uploadStreamsImpl(streams, size)
    }

    private fun uploadStreams(streams: List<Uri>) {
        if (Utils.safeIsEmpty(streams)) return

        val size = Settings.get()
                .main()
                .uploadImageSize

        if (isNull(size)) {
            view?.showImageSizeSelectDialog(streams)
        } else {
            uploadStreamsImpl(streams, size!!)
        }
    }

    @OnGuiCreated
    private fun resolveResumePeer() {
        view?.notifyChatResume(accountId, peerId, mPeer.title, mPeer.avaUrl) // TODO: 15.08.2017
    }

    private fun uploadStreamsImpl(streams: List<Uri>, size: Int) {
        mOutConfig.uploadFiles = null

        view?.resetUploadImages()

        if (mDraftMessageId == null) {
            mDraftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, peerId, mDraftMessageText)
                    .blockingGet()
        }

        val destination = UploadDestination.forMessage(mDraftMessageId!!)
        val intents = ArrayList<UploadIntent>(streams.size)

        for (uri in streams) {
            intents.add(UploadIntent(messagesOwnerId, destination)
                    .setAutoCommit(true)
                    .setFileUri(uri)
                    .setSize(size))
        }

        uploadManager.enqueue(intents)
    }

    fun fireUploadCancelClick() {
        mOutConfig.uploadFiles = null
    }

    @OnGuiCreated
    private fun resolveInputImagesUploading() {
        if (nonEmpty(mOutConfig.uploadFiles)) {
            uploadStreams(mOutConfig.uploadFiles)
        }
    }

    fun fireChatTitleTyped(newValue: String) {
        val chatId = Peer.fromChatId(peerId)

        appendDisposable(messagesInteractor.changeChatTitle(this.messagesOwnerId, chatId, newValue)
                .fromIOToMain()
                .subscribe(dummy(), Consumer { t -> showError(view, t) }))
    }

    fun fireForwardToHereClick(messages: ArrayList<Message>) {
        mOutConfig.models.append(FwdMessages(messages))

        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    fun fireForwardToAnotherClick(messages: ArrayList<Message>) {
        view?.forwardMessagesToAnotherConversation(messages, messagesOwnerId) // TODO: 15.08.2017
    }

    public override fun onActionModeForwardClick() {
        val selected = getSelected(data)
        if (nonEmpty(selected)) {
            view?.diplayForwardTypeSelectDialog(selected)
        }
    }

    @OnGuiCreated
    private fun resolveOptionMenu() {
        val chat = isGroupChat

        var isPlusEncryption = false
        if (isEncryptionEnabled) {
            isPlusEncryption = Settings.get()
                    .security()
                    .getEncryptionLocationPolicy(messagesOwnerId, peerId) == KeyLocationPolicy.RAM
        }

        view?.configOptionMenu(chat, chat, chat, isEncryptionSupport, isEncryptionEnabled, isPlusEncryption, isEncryptionSupport)
    }

    fun fireEncriptionStatusClick() {
        if (!isEncryptionEnabled && !Settings.get().security().isKeyEncryptionPolicyAccepted) {
            view?.showEncryptionDisclaimerDialog(REQUEST_CODE_ENABLE_ENCRYPTION)
            return
        }

        onEncryptionToggleClick()
    }

    private fun onEncryptionToggleClick() {
        val enable = isEncryptionEnabled
        if (enable) {
            Settings.get().security().disableMessageEncryption(messagesOwnerId, peerId)
            resolveOptionMenu()
        } else {
            view!!.showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_ENABLE_ENCRYPTION)
        }
    }

    private fun fireKeyStoreSelected(requestCode: Int, @KeyLocationPolicy policy: Int) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_ENCRYPTION -> onEnableEncryptionKeyStoreSelected(policy)
            REQUEST_CODE_KEY_EXCHANGE -> KeyExchangeService.iniciateKeyExchangeSession(applicationContext, messagesOwnerId, peerId, policy)
        }
    }

    fun fireDiskKeyStoreSelected(requestCode: Int) {
        fireKeyStoreSelected(requestCode, KeyLocationPolicy.PERSIST)
    }

    fun fireRamKeyStoreSelected(requestCode: Int) {
        fireKeyStoreSelected(requestCode, KeyLocationPolicy.RAM)
    }

    private fun onEnableEncryptionKeyStoreSelected(@KeyLocationPolicy policy: Int) {
        appendDisposable(Stores.getInstance()
                .keys(policy)
                .getKeys(messagesOwnerId, peerId)
                .fromIOToMain()
                .subscribe({ aesKeyPairs -> fireEncriptionEnableClick(policy, aesKeyPairs) }, { Analytics.logUnexpectedError(it) }))
    }

    private fun fireEncriptionEnableClick(@KeyLocationPolicy policy: Int, pairs: List<AesKeyPair>) {
        if (safeIsEmpty(pairs)) {
            view?.displayIniciateKeyExchangeQuestion(policy)
        } else {
            Settings.get().security().enableMessageEncryption(messagesOwnerId, peerId, policy)
            resolveOptionMenu()
        }
    }

    fun fireIniciateKeyExchangeClick(@KeyLocationPolicy policy: Int) {
        KeyExchangeService.iniciateKeyExchangeSession(App.getInstance(), messagesOwnerId, peerId, policy)
    }

    override fun saveState(outState: Bundle) {
        super.saveState(outState)
        outState.putParcelable(SAVE_PEER, mPeer)
        outState.putString(SAVE_DRAFT_MESSAGE_TEXT, mDraftMessageText)
        outState.putInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT, mDraftMessageDbAttachmentsCount)
        outState.putParcelable(SAVE_CONFIG, mOutConfig)

        mDraftMessageId?.run {
            outState.putInt(SAVE_DRAFT_MESSAGE_ID, this)
        }
    }

    private fun restoreFromInstanceState(state: Bundle) {
        mDraftMessageText = state.getString(SAVE_DRAFT_MESSAGE_TEXT)
        mDraftMessageDbAttachmentsCount = state.getInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT)

        if (state.containsKey(SAVE_DRAFT_MESSAGE_ID)) {
            mDraftMessageId = state.getInt(SAVE_DRAFT_MESSAGE_ID)
        }
    }

    fun fireStickerSendClick(stickerId: Int) {
        val sticker = Sticker(stickerId)

        val builder = SaveMessageBuilder(messagesOwnerId, peerId).attach(sticker)
        sendMessage(builder)
    }

    fun fireKeyExchangeClick() {
        if (!Settings.get().security().isKeyEncryptionPolicyAccepted) {
            view?.showEncryptionDisclaimerDialog(REQUEST_CODE_KEY_EXCHANGE)
            return
        }

        if (isEncryptionSupport) {
            view?.showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_KEY_EXCHANGE)
        }
    }

    fun reInitWithNewPeer(newAccountId: Int, newMessagesOwnerId: Int, newPeerId: Int, title: String) {
        saveDraftMessageBody()

        val oldMessageOwnerId = this.messagesOwnerId
        val oldPeerId = peerId

        this.mPeer = Peer(newPeerId).setTitle(title)

        if (isGuiResumed) {
            Processors.realtimeMessages().registerNotificationsInterceptor(id, Pair.create(messagesOwnerId, peerId))
        }

        checkLongpoll()

        resolveAccountHotSwapSupport()
        resetDatabaseLoading()

        super.getData().clear()
        safeNotifyDataChanged()

        loadAllCachedData()
        requestAtStart()
        updateSubtitle()

        resolveToolbarTitle()
        resolveToolbarSubtitle()
        resolveOptionMenu()
        resolveResumePeer()

        mTextingNotifier = TextingNotifier(messagesOwnerId)

        this.mDraftMessageId = null
        this.mDraftMessageText = null
        this.mDraftMessageDbAttachmentsCount = 0

        val needToRestoreDraftMessageBody = isEmpty(mOutConfig.initialText)
        if (!needToRestoreDraftMessageBody) {
            this.mDraftMessageText = mOutConfig.initialText
        }

        tryToRestoreDraftMessage(!needToRestoreDraftMessageBody)
    }

    fun fireTermsOfUseAcceptClick(requestCode: Int) {
        Settings.get().security().isKeyEncryptionPolicyAccepted = true

        when (requestCode) {
            REQUEST_CODE_KEY_EXCHANGE -> if (isEncryptionSupport) {
                view?.showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_KEY_EXCHANGE)
            }

            REQUEST_CODE_ENABLE_ENCRYPTION -> onEncryptionToggleClick()
        }
    }

    fun fireSendClickFromAttachmens() {
        fireSendClick()
    }

    private class ToolbarSubtitleHandler internal constructor(prensenter: ChatPrensenter) : Handler() {

        private var mReference: WeakReference<ChatPrensenter> = WeakReference(prensenter)

        override fun handleMessage(msg: android.os.Message) {
            mReference.get()?.run {
                when (msg.what) {
                    RESTORE_TOLLBAR -> resolveToolbarSubtitle()
                }
            }
        }

        internal fun release() {
            removeMessages(RESTORE_TOLLBAR)
        }

        internal fun restoreToolbarWithDelay() {
            sendMessageDelayed(android.os.Message.obtain(this, RESTORE_TOLLBAR), 3000)
        }

        companion object {
            private const val RESTORE_TOLLBAR = 12
        }
    }

    companion object {

        private const val COUNT = 30
        private const val RECORD_EXT_MP3 = "mp3"

        private const val SAVE_PEER = "save_peer"
        private const val SAVE_DRAFT_MESSAGE_TEXT = "save_draft_message_text"
        private const val SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT = "save_draft_message_attachments_count"
        private const val SAVE_DRAFT_MESSAGE_ID = "save_draft_message_id"
        private const val SAVE_CONFIG = "save_config"

        private const val REQUEST_CODE_ENABLE_ENCRYPTION = 1
        private const val REQUEST_CODE_KEY_EXCHANGE = 2

        private val MESSAGES_COMPARATOR = Comparator<Message> { rhs, lhs ->
            // соблюдаем сортировку как при запросе в бд

            if (lhs.status == rhs.status) {
                return@Comparator lhs.id.compareTo(rhs.id)
            }

            lhs.status.compareTo(rhs.status)
        }
    }
}
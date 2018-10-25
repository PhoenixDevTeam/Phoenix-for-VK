package biz.dealnote.messenger.mvp.presenter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import biz.dealnote.messenger.*
import biz.dealnote.messenger.crypt.AesKeyPair
import biz.dealnote.messenger.crypt.KeyExchangeService
import biz.dealnote.messenger.crypt.KeyLocationPolicy
import biz.dealnote.messenger.crypt.KeyPairDoesNotExistException
import biz.dealnote.messenger.db.Stores
import biz.dealnote.messenger.domain.IAttachmentsRepository
import biz.dealnote.messenger.domain.IMessagesRepository
import biz.dealnote.messenger.domain.Mode
import biz.dealnote.messenger.domain.Repository
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
import biz.dealnote.messenger.upload.*
import biz.dealnote.messenger.util.*
import biz.dealnote.messenger.util.RxUtils.*
import biz.dealnote.messenger.util.Utils.*
import biz.dealnote.mvp.reflect.OnGuiCreated
import io.reactivex.disposables.Disposables
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
class ChatPrensenter(accountId: Int, private val messagesOwnerId: Int,
                     initialPeer: Peer,
                     config: ChatConfig, savedInstanceState: Bundle?) : AbsMessageListPresenter<IChatView>(accountId, savedInstanceState) {

    private var peer: Peer
    private var subtitle: String? = null
    private val audioRecordWrapper: AudioRecordWrapper
    private var endOfContent: Boolean = false
    private var outConfig: ChatConfig
    private var draftMessageText: String? = null
    private var draftMessageId: Int? = null
    private var textingNotifier: TextingNotifier
    private var toolbarSubtitleHandler: ToolbarSubtitleHandler = ToolbarSubtitleHandler(this)
    private var draftMessageDbAttachmentsCount: Int = 0

    private var recordingLookup: Lookup

    private val messagesRepository: IMessagesRepository = Repository.messages
    private val longpollManager: ILongpollManager = LongpollInstance.get()
    private val uploadManager: IUploadManager = Injection.provideUploadManager()

    private var cacheLoadingDisposable = Disposables.disposed()
    private var netLoadingDisposable = Disposables.disposed()
    private var fetchConversationDisposable = Disposables.disposed()

    private var conversation: Conversation? = null

    private var isLoadingFromDbNow = false
    private var isLoadingFromNetNow = false

    private val isLoadingNow: Boolean
        get() = isLoadingFromDbNow || isLoadingFromNetNow

    private val isRecordingNow: Boolean
        get() {
            val status = audioRecordWrapper.recorderStatus
            return status == Recorder.Status.PAUSED || status == Recorder.Status.RECORDING_NOW
        }

    private val isGroupChat: Boolean
        get() = Peer.isGroupChat(peerId)

    private val peerId: Int
        get() = peer.id

    private val isEncryptionSupport: Boolean
        get() = Peer.isUser(peerId) && peerId != messagesOwnerId

    private val isEncryptionEnabled: Boolean
        get() = Settings.get()
                .security()
                .isMessageEncryptionEnabled(messagesOwnerId, peerId)

    private var currentPhotoCameraUri: Uri? = null

    init {
        audioRecordWrapper = AudioRecordWrapper.Builder(App.getInstance())
                .setFileExt(RECORD_EXT_MP3)
                .build()

        if (savedInstanceState == null) {
            peer = initialPeer
            outConfig = config

            if (config.initialText.nonEmpty()) {
                draftMessageText = config.initialText
            }
        } else {
            peer = savedInstanceState.getParcelable(SAVE_PEER)
            outConfig = savedInstanceState.getParcelable(SAVE_CONFIG)
            currentPhotoCameraUri = savedInstanceState.getParcelable(SAVE_CAMERA_FILE_URI)
            restoreFromInstanceState(savedInstanceState)
        }

        fetchConversationThenCachedThenActual()

        if (savedInstanceState == null) {
            tryToRestoreDraftMessage(draftMessageText.isNullOrEmpty())
        }

        resolveAccountHotSwapSupport()
        textingNotifier = TextingNotifier(messagesOwnerId)

        val predicate = Predicate<IAttachmentsRepository.IBaseEvent> { event ->
            draftMessageId != null
                    && event.accountId == messagesOwnerId
                    && event.attachToId == draftMessageId
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

        recordingLookup = Lookup(1000)
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

                        if (m != null && peerId == m.peerId) {
                            onRealtimeMessageReceived(m)
                        }
                    }
                })

        appendDisposable(uploadManager.observeAdding()
                .toMainThread()
                .subscribe(Consumer { onUploadAdded(it) }, ignore()))

        appendDisposable(uploadManager.observeDeleting(true)
                .toMainThread()
                .subscribe(Consumer { onUploadRemoved(it) }, ignore()))

        appendDisposable(uploadManager.observeResults()
                .toMainThread()
                .subscribe(Consumer { onUploadResult(it) }, ignore()))

        appendDisposable(uploadManager.obseveStatus()
                .toMainThread()
                .subscribe(Consumer { onUploadStatusChange(it) }, ignore()))

        appendDisposable(uploadManager.observeProgress()
                .toMainThread()
                .subscribe(Consumer { onUploadProgressUpdate(it) }, ignore()))

        appendDisposable(messagesRepository.observePeerUpdates()
                .toMainThread()
                .subscribe(Consumer { onPeerUpdate(it) }, ignore()))

        updateSubtitle()
    }

    private fun onPeerUpdate(updates: List<PeerUpdate>) {
        var requireListUpdate = false

        for (update in updates) {
            if (update.accountId != messagesOwnerId || update.peerId != peerId) continue

            update.readIn?.run {
                conversation?.inRead = messageId
                conversation?.unreadCount = unreadCount
                lastReadId.incoming = messageId
                requireListUpdate = true
            }

            update.readOut?.run {
                conversation?.outRead = messageId
                lastReadId.outgoing = messageId
                requireListUpdate = true
            }
        }

        if (requireListUpdate) {
            view?.notifyDataChanged()
        }
    }

    private fun fetchConversationThenCachedThenActual() {
        fetchConversationDisposable = messagesRepository.getConversationSingle(messagesOwnerId, peer.id, Mode.ANY)
                .fromIOToMain()
                .subscribe({ onConveractionFetched(it) }, { onConversationFetchFail(it) })
    }

    private fun onConversationFetchFail(throwable: Throwable) {
        showError(view, throwable)
    }

    private fun onConveractionFetched(data: Conversation) {
        conversation = data

        resolvePinnedMessageView()

        lastReadId.incoming = data.inRead
        lastReadId.outgoing = data.outRead

        loadAllCachedData()
        requestAtStart()
    }

    private fun onUploadProgressUpdate(data: List<IUploadManager.IProgressUpdate>) {
        edited?.run {
            for (update in data) {
                val index = attachments.indexOfFirst {
                    it.attachment is Upload && (it.attachment as Upload).id == update.id
                }

                if (index != -1) {
                    val upload = attachments[index].attachment as Upload
                    if (upload.status == Upload.STATUS_UPLOADING) {
                        upload.progress = update.progress
                        view?.notifyEditUploadProgressUpdate(index, update.progress)
                    }
                }
            }
        }
    }

    private fun onUploadStatusChange(upload: Upload) {
        edited?.run {
            val index = attachments.indexOfFirst {
                it.attachment is Upload && (it.attachment as Upload).id == upload.id
            }

            if (index != -1) {
                (attachments[index].attachment as Upload).apply {
                    status = upload.status
                    errorText = upload.errorText
                }

                view?.notifyEditAttachmentChanged(index)
            }
        }
    }

    private fun onUploadResult(pair: Pair<Upload, UploadResult<*>>) {
        edited?.run {
            val destination = pair.first.destination

            if (message.id == destination.id && destination.method == Method.PHOTO_TO_MESSAGE) {
                val photo: Photo = pair.second.result as Photo
                val sizeBefore = attachments.size

                attachments.add(AttachmenEntry(true, photo))
                view?.notifyEditAttachmentsAdded(sizeBefore, 1)
                resolveAttachmentsCounter()
            }
        }
    }

    private fun onUploadRemoved(ids: IntArray) {
        edited?.run {
            for (id in ids) {
                val index = attachments.indexOfFirst {
                    it.attachment is Upload && (it.attachment as Upload).id == id
                }

                if (index != -1) {
                    attachments.removeAt(index)
                    view?.notifyEditAttachmentRemoved(index)
                }
            }
        }
    }

    private fun onUploadAdded(uploads: List<Upload>) {
        edited?.run {
            val filtered = uploads
                    .asSequence()
                    .filter { u ->
                        u.destination.id == message.id && u.destination.method == Method.PHOTO_TO_MESSAGE
                    }.map {
                        AttachmenEntry(true, it)
                    }.toList()

            if (filtered.isNotEmpty()) {
                attachments.addAll(0, filtered)
                view?.notifyEditAttachmentsAdded(0, filtered.size)
            }
        }
    }

    @OnGuiCreated
    private fun resolvePinnedMessageView() {
        view?.displayPinnedMessage(conversation?.pinned)
    }

    @OnGuiCreated
    private fun resolveEditedMessageViews() {
        view?.displayEditingMessage(edited?.message)
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
                //RealtimeAction.MESSAGES_READ -> {
                //    val read = action as MessagesRead
                //    onLongpollMessagesRead(messagesOwnerId, read.peerId, read.isOut, read.toMessageId)
                //}
            }
        }
    }

    /*private fun onLongpollMessagesRead(accountId: Int, peerId: Int, out: Boolean, localId: Int) {
        if (messagesOwnerId != accountId || peerId != peerId) return

        conversation?.run {
            if (out) {
                outRead = localId
                lastReadId.outgoing = localId
            } else {
                inRead = localId
                lastReadId.incoming = localId
            }
        }

        view?.notifyDataChanged()
    }*/

    private fun onUserIsOffline(userOffline: UserOffline) {
        if (isChatWithUser(userOffline.userId)) {
            val lastSeeenUnixtime = if (userOffline.isByTimeout) Unixtime.now() - 15 * 60 else Unixtime.now()
            subtitle = getString(R.string.last_seen_sex_unknown, AppTextUtils.getDateFromUnixTime(lastSeeenUnixtime))
            resolveToolbarSubtitle()
        }
    }

    private fun onUserIsOnline(userOnline: UserOnline) {
        if (isChatWithUser(userOnline.userId)) {
            subtitle = getString(R.string.online)
            resolveToolbarSubtitle()
        }
    }

    private fun onUserWriteInDialog(writeText: WriteText) {
        if (peerId == writeText.peerId && !isGroupChat) {
            displayUserTextingInToolbar()
        }
    }

    private fun onRepositoryAttachmentsRemoved() {
        draftMessageDbAttachmentsCount--
        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    private fun onRepositoryAttachmentsAdded(count: Int) {
        draftMessageDbAttachmentsCount += count
        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    private fun loadAllCachedData() {
        setCacheLoadingNow(true)
        cacheLoadingDisposable = messagesRepository.getCachedPeerMessages(messagesOwnerId, peer.id)
                .fromIOToMain()
                .subscribe({ onCachedDataReceived(it) }, { onCachedDataReceived(Collections.emptyList()) })
    }

    private fun onCachedDataReceived(data: List<Message>) {
        setCacheLoadingNow(false)
        onAllDataLoaded(data, false)
    }

    private fun onNetDataReceived(messages: List<Message>, startMessageId: Int?) {
        cacheLoadingDisposable.dispose()

        isLoadingFromDbNow = false
        endOfContent = messages.isEmpty()

        setNetLoadingNow(false)
        onAllDataLoaded(messages, startMessageId != null)
    }

    private fun onAllDataLoaded(messages: List<Message>, appendToList: Boolean) {
        val all = !appendToList

        //сохранение выделенных сообщений
        if (all) {
            val selectedList = data.filter {
                it.isSelected
            }

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

        if (all && data.isNotEmpty()) {
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
        view?.setupLoadUpHeaderState(if (loading) LoadMoreState.LOADING else if (endOfContent) LoadMoreState.INVISIBLE else LoadMoreState.CAN_LOAD_MORE)
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
        netLoadingDisposable = messagesRepository.getPeerMessages(messagesOwnerId, peerId, COUNT, null, startMessageId, true)
                .fromIOToMain()
                .subscribe({ messages -> onNetDataReceived(messages, startMessageId) }, { this.onMessagesGetError(it) })
    }

    private fun onMessagesGetError(t: Throwable) {
        setNetLoadingNow(false)
        showError(view, getCauseIfRuntime(t))
    }

    private fun requestMore() {
        val lastId = if (data.size > 0) data[data.size - 1].id else null
        requestFromNet(lastId)
    }

    private fun onMessagesRestoredSuccessfully(id: Int) {
        data.find {
            it.id == id
        }?.run {
            isDeleted = false
            view?.notifyDataChanged()
        }
    }

    fun fireDraftMessageTextEdited(s: String) {
        edited?.run {
            this.body = s
            return
        }

        val oldState = canSendNormalMessage()
        draftMessageText = s
        val newState = canSendNormalMessage()

        if (oldState != newState) {
            setupSendButtonState(newState, true)
        }

        readAllUnreadMessagesIfExists()

        textingNotifier.notifyAboutTyping(peerId)
    }

    fun fireSendClick() {
        if (canSendNormalMessage()) {
            sendImpl()
        }
    }

    private fun sendImpl() {
        val securitySettings = Settings.get().security()

        val trimmedBody = AppTextUtils.safeTrim(draftMessageText, null)
        val encryptionEnabled = securitySettings.isMessageEncryptionEnabled(messagesOwnerId, peerId)

        @KeyLocationPolicy
        var keyLocationPolicy = KeyLocationPolicy.PERSIST
        if (encryptionEnabled) {
            keyLocationPolicy = securitySettings.getEncryptionLocationPolicy(messagesOwnerId, peerId)
        }

        val builder = SaveMessageBuilder(messagesOwnerId, peer.id)
                .also {
                    it.body = trimmedBody
                    it.draftMessageId = draftMessageId
                    it.isRequireEncryption = encryptionEnabled
                    it.keyLocationPolicy = keyLocationPolicy
                }

        val fwds = ArrayList<Message>()

        for (model in outConfig.models) {
            if (model is FwdMessages) {
                fwds.addAll(model.fwds)
            } else {
                builder.attach(model)
            }
        }

        builder.forwardMessages = fwds

        outConfig.models.clear()
        outConfig.initialText = null

        draftMessageId = null
        draftMessageText = null
        draftMessageDbAttachmentsCount = 0

        view?.resetInputAttachments()

        resolveAttachmentsCounter()
        resolveDraftMessageText()
        resolveSendButtonState()

        sendMessage(builder)

        if (outConfig.isCloseOnSend) {
            view?.doCloseAfterSend()
        }
    }

    @SuppressLint("CheckResult")
    private fun sendMessage(builder: SaveMessageBuilder) {
        messagesRepository.put(builder)
                .fromIOToMain()
                .doOnSuccess { _ -> startSendService() }
                .subscribe(WeakConsumer(messageSavedConsumer), WeakConsumer(messageSaveFailConsumer))
    }

    private val messageSavedConsumer: Consumer<Message> = Consumer { onMessageSaveSuccess(it) }
    private val messageSaveFailConsumer: Consumer<Throwable> = Consumer { onMessageSaveError(it) }

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
        edited?.run {
            view?.showEditAttachmentsDialog(attachments)
            return
        }

        if (draftMessageId == null) {
            draftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, peerId, draftMessageText)
                    .blockingGet()
        }

        val destination = UploadDestination.forMessage(draftMessageId!!)
        view?.goToMessageAttachmentsEditor(accountId, messagesOwnerId, destination, draftMessageText, outConfig.models) // TODO: 15.08.2017
    }

    private fun canSendNormalMessage(): Boolean {
        return calculateAttachmentsCount() > 0 || trimmedNonEmpty(draftMessageText) || nowUploadingToEditingMessage()
    }

    @OnGuiCreated
    private fun resolveEmptyTextVisibility() {
        view?.setEmptyTextVisible(safeIsEmpty(data) && !isLoadingNow)
    }

    private fun nowUploadingToEditingMessage(): Boolean {
        val messageId = draftMessageId ?: return false

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
        edited?.run {
            view?.displayDraftMessageAttachmentsCount(calculateAttachmentsCount(this))
        } ?: run {
            view?.displayDraftMessageAttachmentsCount(calculateAttachmentsCount())
        }
    }

    @OnGuiCreated
    private fun resolveDraftMessageText() {
        edited?.run {
            view?.displayDraftMessageText(body)
        } ?: run {
            view?.displayDraftMessageText(draftMessageText)
        }
    }

    @OnGuiCreated
    private fun resolveToolbarTitle() {
        view?.displayToolbarTitle(peer.title)
    }

    fun fireRecordCancelClick() {
        audioRecordWrapper.stopRecording()
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
            val file = audioRecordWrapper.stopRecordingAndReceiveFile()
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
                val isRecorderPaused = audioRecordWrapper.recorderStatus == Recorder.Status.PAUSED
                if (!isRecorderPaused) {
                    audioRecordWrapper.pause()
                } else {
                    audioRecordWrapper.doRecord()
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
        val paused = audioRecordWrapper.recorderStatus == Recorder.Status.PAUSED
        val available = audioRecordWrapper.isPauseSupported
        view?.setupRecordPauseButton(available, !paused)
    }

    fun fireRecordPermissionsResolved() {
        if (hasAudioRecordPermissions()) {
            startRecordImpl()
        }
    }

    private fun startRecordImpl() {
        try {
            audioRecordWrapper.doRecord()
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
            recordingLookup.start()
        } else {
            recordingLookup.stop()
        }
    }

    @OnGuiCreated
    private fun resolveRecordingTimeView() {
        if (isRecordingNow) {
            view?.displayRecordingDuration(audioRecordWrapper.currentRecordDuration)
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
        if (message.peerId != peer.id || messagesOwnerId != message.accountId) {
            return
        }

        if (message.isChatTitleUpdate) {
            peer.title = message.actionText
            resolveToolbarTitle()
        }

        val index = data.indexOfFirst {
            it.id == message.id
        }

        if (index != -1) {
            data.removeAt(index)
        }

        if (message.isOut && message.randomId > 0) {
            val unsentIndex = data.indexOfFirst {
                it.randomId == message.randomId && !it.isSent
            }

            if (unsentIndex != -1) {
                data.removeAt(unsentIndex)
            }
        }

        addMessageToList(message)
        view?.notifyDataChanged()
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
        toolbarSubtitleHandler.restoreToolbarWithDelay()
    }

    private fun updateSubtitle() {
        subtitle = null

        val peerType = Peer.getType(peerId)

        when (peerType) {
            Peer.CHAT, Peer.GROUP -> {
                subtitle = null
                resolveToolbarSubtitle()
            }

            Peer.USER -> appendDisposable(Stores.getInstance()
                    .owners()
                    .getLocalizedUserActivity(messagesOwnerId, Peer.toUserId(peerId))
                    .compose(RxUtils.applyMaybeIOToMainSchedulers())
                    .subscribe({ s ->
                        subtitle = s
                        resolveToolbarSubtitle()
                    }, { Analytics.logUnexpectedError(it) }, { this.resolveToolbarSubtitle() }))
        }
    }

    @OnGuiCreated
    private fun resolveToolbarSubtitle() {
        view?.displayToolbarSubtitle(subtitle)
    }

    private fun checkLongpoll() {
        if (isGuiResumed && accountId != ISettings.IAccountsSettings.INVALID_ID) {
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

    private fun calculateAttachmentsCount(message: EditedMessage): Int {
        var count = 0

        for (entry in message.attachments) {
            if (entry.attachment is FwdMessages) {
                count += (entry.attachment as FwdMessages).fwds.size
            } else if (entry.attachment !is Upload) {
                count++
            }
        }

        return count
    }

    private fun calculateAttachmentsCount(): Int {
        var outConfigCount = 0
        for (model in outConfig.models) {
            if (model is FwdMessages) {
                outConfigCount += model.fwds.size
            } else {
                outConfigCount++
            }
        }

        return outConfigCount + draftMessageDbAttachmentsCount
    }

    private fun onDraftMessageRestored(message: DraftMessage, ignoreBody: Boolean) {
        draftMessageDbAttachmentsCount = message.attachmentsCount
        draftMessageId = message.id

        if (!ignoreBody) {
            draftMessageText = message.body
        }

        resolveAttachmentsCounter()
        resolveSendButtonState()
        resolveDraftMessageText()
    }

    private fun resolveAccountHotSwapSupport() {
        setSupportAccountHotSwap(!Peer.isGroupChat(peerId))
    }

    override fun onDestroyed() {
        cacheLoadingDisposable.dispose()
        netLoadingDisposable.dispose()
        fetchConversationDisposable.dispose()

        saveDraftMessageBody()

        toolbarSubtitleHandler.release()

        recordingLookup.stop()
        recordingLookup.setCallback(null)

        textingNotifier.shutdown()
        super.onDestroyed()
    }

    private fun saveDraftMessageBody() {
        Stores.getInstance()
                .messages()
                .saveDraftMessageBody(messagesOwnerId, peerId, draftMessageText)
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
        val last = if (data.nonEmpty()) data[0] else return

        if (!last.isOut && last.id > lastReadId.incoming) {
            lastReadId.incoming = last.id

            view?.notifyDataChanged()

            appendDisposable(messagesRepository.markAsRead(messagesOwnerId, peer.id, last.id)
                    .fromIOToMain()
                    .subscribe(dummy(), Consumer { t -> showError(view, t) }))
        }
    }

    fun fireMessageRestoreClick(message: Message) {
        restoreMessage(message.id)
    }

    private fun restoreMessage(messageId: Int) {
        appendDisposable(messagesRepository.restoreMessage(this.messagesOwnerId, messageId)
                .fromIOToMain()
                .subscribe({ onMessagesRestoredSuccessfully(messageId) }, { t -> showError(view, t) }))
    }

    fun fireEditMessageResult(accompanyingModels: ModelsBundle) {
        outConfig.models = accompanyingModels

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

        if (sent.nonEmpty()) {
            appendDisposable(messagesRepository.deleteMessages(messagesOwnerId, sent)
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

        appendDisposable(messagesRepository.removeChatUser(accountId, chatId, accountId)
                .fromIOToMain()
                .subscribe(dummy(), Consumer { t -> showError(view, t) }))
    }

    fun fireChatTitleClick() {
        view?.showChatTitleChangeDialog(peer.title)
    }

    fun fireChatMembersClick() {
        view?.goToChatMembers(accountId, Peer.toChatId(peerId))
    }

    fun fireDialogAttachmentsClick() {
        view?.goToConversationAttachments(accountId, peerId) // TODO: 15.08.2017
    }

    fun fireSearchClick() {
        view?.goToSearchMessage(accountId, peer) // TODO: 15.08.2017
    }

    fun fireImageUploadSizeSelected(streams: List<Uri>, size: Int) {
        uploadStreamsImpl(streams, size)
    }

    private fun uploadStreams(streams: List<Uri>) {
        if (streams.nullOrEmpty()) return

        val size = Settings.get()
                .main()
                .uploadImageSize

        if (size == null) {
            view?.showImageSizeSelectDialog(streams)
        } else {
            uploadStreamsImpl(streams, size)
        }
    }

    @OnGuiCreated
    private fun resolveResumePeer() {
        view?.notifyChatResume(accountId, peerId, peer.title, peer.avaUrl) // TODO: 15.08.2017
    }

    private fun uploadStreamsImpl(streams: List<Uri>, size: Int) {
        outConfig.uploadFiles = null

        view?.resetUploadImages()

        if (draftMessageId == null) {
            draftMessageId = Stores.getInstance()
                    .messages()
                    .saveDraftMessageBody(messagesOwnerId, peerId, draftMessageText)
                    .blockingGet()
        }

        val destination = UploadDestination.forMessage(draftMessageId!!)
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
        outConfig.uploadFiles = null
    }

    @OnGuiCreated
    private fun resolveInputImagesUploading() {
        if (outConfig.uploadFiles.nonEmpty()) {
            uploadStreams(outConfig.uploadFiles)
        }
    }

    fun fireChatTitleTyped(newValue: String) {
        val chatId = Peer.fromChatId(peerId)

        appendDisposable(messagesRepository.changeChatTitle(this.messagesOwnerId, chatId, newValue)
                .fromIOToMain()
                .subscribe(dummy(), Consumer { t -> showError(view, t) }))
    }

    fun fireForwardToHereClick(messages: ArrayList<Message>) {
        outConfig.models.append(FwdMessages(messages))

        resolveAttachmentsCounter()
        resolveSendButtonState()
    }

    fun fireForwardToAnotherClick(messages: ArrayList<Message>) {
        view?.forwardMessagesToAnotherConversation(messages, messagesOwnerId) // TODO: 15.08.2017
    }

    public override fun onActionModeForwardClick() {
        val selected = getSelected(data)
        if (selected.isNotEmpty()) {
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
        if (isEncryptionEnabled) {
            Settings.get().security().disableMessageEncryption(messagesOwnerId, peerId)
            resolveOptionMenu()
        } else {
            view?.showEncryptionKeysPolicyChooseDialog(REQUEST_CODE_ENABLE_ENCRYPTION)
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
        if (pairs.nullOrEmpty()) {
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
        outState.putParcelable(SAVE_PEER, peer)
        outState.putString(SAVE_DRAFT_MESSAGE_TEXT, draftMessageText)
        outState.putInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT, draftMessageDbAttachmentsCount)
        outState.putParcelable(SAVE_CONFIG, outConfig)
        outState.putParcelable(SAVE_CAMERA_FILE_URI, currentPhotoCameraUri)

        draftMessageId?.run {
            outState.putInt(SAVE_DRAFT_MESSAGE_ID, this)
        }
    }

    private fun restoreFromInstanceState(state: Bundle) {
        draftMessageText = state.getString(SAVE_DRAFT_MESSAGE_TEXT)
        draftMessageDbAttachmentsCount = state.getInt(SAVE_DRAFT_MESSAGE_ATTACHMENTS_COUNT)

        if (state.containsKey(SAVE_DRAFT_MESSAGE_ID)) {
            draftMessageId = state.getInt(SAVE_DRAFT_MESSAGE_ID)
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

        this.peer = Peer(newPeerId).setTitle(title)

        if (isGuiResumed) {
            Processors.realtimeMessages().registerNotificationsInterceptor(id, Pair.create(messagesOwnerId, peerId))
        }

        checkLongpoll()

        resolveAccountHotSwapSupport()

        netLoadingDisposable.dispose()
        cacheLoadingDisposable.dispose()
        fetchConversationDisposable.dispose()

        super.getData().clear()
        view?.notifyDataChanged()

        loadAllCachedData()
        requestAtStart()
        updateSubtitle()

        resolveToolbarTitle()
        resolveToolbarSubtitle()
        resolveOptionMenu()
        resolveResumePeer()

        textingNotifier.shutdown()
        textingNotifier = TextingNotifier(messagesOwnerId)

        draftMessageId = null
        draftMessageText = null
        draftMessageDbAttachmentsCount = 0

        val needToRestoreDraftMessageBody = outConfig.initialText.isNullOrEmpty()
        if (!needToRestoreDraftMessageBody) {
            draftMessageText = outConfig.initialText
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

    private var edited: EditedMessage? = null

    fun fireActionModeEditClick() {
        val m = data.find {
            it.isSelected
        }

        edited = if (m != null) EditedMessage(m) else null

        resolveDraftMessageText()
        resolveAttachmentsCounter()
        resolveEditedMessageViews()
    }

    private fun cancelMessageEditing(): Boolean {
        edited?.run {
            val destination = UploadDestination.forMessage(message.id)

            edited = null
            resolveDraftMessageText()
            resolveAttachmentsCounter()
            resolveEditedMessageViews()

            uploadManager.cancelAll(accountId, destination)
            return true
        }

        return false
    }

    fun fireCancelEditingClick() {
        cancelMessageEditing()
    }

    fun onBackPressed(): Boolean {
        return !cancelMessageEditing()
    }

    fun fireEditMessageSaveClick() {
        edited?.run {
            val models = ArrayList<AbsModel>()
            var keepForward = false

            for (entry in attachments) {
                when (entry.attachment) {
                    is FwdMessages -> keepForward = true
                    is Upload -> {
                        view?.showError(R.string.upload_not_resolved_exception_message)
                        return
                    }
                    else -> models.add(entry.attachment)
                }
            }

            appendDisposable(messagesRepository.edit(accountId, message, body, models, keepForward)
                    .fromIOToMain()
                    .subscribe({ onMessageEdited(it) }, { t -> onMessageEditFail(t) }))
        }
    }

    private fun onMessageEditFail(throwable: Throwable) {
        showError(view, throwable)
    }

    private fun onMessageEdited(message: Message) {
        edited = null
        resolveAttachmentsCounter()
        resolveDraftMessageText()
        resolveEditedMessageViews()

        val index = data.indexOfFirst {
            it.id == message.id
        }

        if (index != -1) {
            data[index] = message
            view?.notifyDataChanged()
        }
    }

    fun fireEditAttachmentRemoved(entry: AttachmenEntry) {
        if (entry.attachment is Upload) {
            uploadManager.cancel((entry.attachment as Upload).id)
            return
        }

        edited?.run {
            val index = attachments.indexOf(entry)
            if (index != -1) {
                attachments.removeAt(index)
                view?.notifyEditAttachmentRemoved(index)
                resolveAttachmentsCounter()
            }
        }
    }

    fun fireEditAddImageClick() {
        view?.startImagesSelection(accountId, messagesOwnerId)
    }

    fun fireEditLocalPhotosSelected(localPhotos: List<LocalPhoto>, imageSize: Int) {
        edited?.run {
            if (localPhotos.isNotEmpty()) {
                val destination = UploadDestination.forMessage(message.id)

                val intents = localPhotos.map {
                    UploadIntent(accountId, destination).apply {
                        isAutoCommit = false
                        fileId = it.imageId
                        fileUri = it.fullImageUri
                        size = imageSize
                    }
                }

                uploadManager.enqueue(intents)
            }
        }
    }

    fun fireEditAttachmentsSelected(models: List<AbsModel>) {
        edited?.run {
            if (models.isNotEmpty()) {
                val additional = models.map {
                    AttachmenEntry(true, it)
                }

                val sizeBefore = attachments.size
                attachments.addAll(additional)
                view?.notifyEditAttachmentsAdded(sizeBefore, additional.size)
                resolveAttachmentsCounter()
            }
        }
    }

    fun fireActionModePinClick() {

    }

    fun onEditAddVideoClick() {
        view?.startVideoSelection(accountId, messagesOwnerId)
    }

    fun onEditAddDocClick() {
        view?.startDocSelection(accountId, messagesOwnerId)
    }

    fun fireEditCameraClick() {
        try {
            val file = FileUtil.createImageFile()
            currentPhotoCameraUri = FileUtil.getExportedUriForFile(applicationContext, file)
            currentPhotoCameraUri?.run {
                view?.startCamera(this)
            }
        } catch (e: IOException) {
            safeShowError(view, e.message)
        }
    }

    fun fireEditPhotoMaked(size: Int) {
        val uri = currentPhotoCameraUri
        currentPhotoCameraUri = null

        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
        applicationContext.sendBroadcast(scanIntent)

        val makedPhoto = LocalPhoto().setFullImageUri(uri)
        fireEditLocalPhotosSelected(listOf(makedPhoto), size)
    }

    private class ToolbarSubtitleHandler internal constructor(prensenter: ChatPrensenter) : Handler(Looper.getMainLooper()) {

        var reference: WeakReference<ChatPrensenter> = WeakReference(prensenter)

        override fun handleMessage(msg: android.os.Message) {
            reference.get()?.run {
                when (msg.what) {
                    RESTORE_TOLLBAR -> resolveToolbarSubtitle()
                }
            }
        }

        fun release() {
            removeMessages(RESTORE_TOLLBAR)
        }

        fun restoreToolbarWithDelay() {
            sendEmptyMessageDelayed(RESTORE_TOLLBAR, 3000)
        }

        companion object {
            const val RESTORE_TOLLBAR = 12
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
        private const val SAVE_CAMERA_FILE_URI = "save_camera_file_uri"

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
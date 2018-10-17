package biz.dealnote.messenger.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputType
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.*
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import biz.dealnote.messenger.Constants
import biz.dealnote.messenger.Extra
import biz.dealnote.messenger.R
import biz.dealnote.messenger.activity.*
import biz.dealnote.messenger.adapter.AttachmentsBottomSheetAdapter
import biz.dealnote.messenger.adapter.AttachmentsViewBinder
import biz.dealnote.messenger.adapter.MessagesAdapter
import biz.dealnote.messenger.api.model.VKApiAttachment
import biz.dealnote.messenger.crypt.KeyLocationPolicy
import biz.dealnote.messenger.dialog.ImageSizeAlertDialog
import biz.dealnote.messenger.fragment.base.PlaceSupportMvpFragment
import biz.dealnote.messenger.fragment.search.SearchContentType
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria
import biz.dealnote.messenger.fragment.sheet.MessageAttachmentsFragment
import biz.dealnote.messenger.listener.BackPressCallback
import biz.dealnote.messenger.listener.OnSectionResumeCallback
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener
import biz.dealnote.messenger.model.*
import biz.dealnote.messenger.model.selection.LocalPhotosSelectableSource
import biz.dealnote.messenger.model.selection.Sources
import biz.dealnote.messenger.model.selection.VkPhotosSelectableSource
import biz.dealnote.messenger.mvp.presenter.ChatPrensenter
import biz.dealnote.messenger.mvp.view.IChatView
import biz.dealnote.messenger.place.PlaceFactory
import biz.dealnote.messenger.settings.CurrentTheme
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.upload.UploadDestination
import biz.dealnote.messenger.util.InputTextDialog
import biz.dealnote.messenger.util.Utils.safeIsEmpty
import biz.dealnote.messenger.util.ViewUtils
import biz.dealnote.messenger.view.InputViewController
import biz.dealnote.messenger.view.LoadMoreFooterHelper
import biz.dealnote.messenger.view.emoji.EmojiconTextView
import biz.dealnote.messenger.view.emoji.StickersGridView
import biz.dealnote.mvp.core.IPresenterFactory
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
class ChatFragment : PlaceSupportMvpFragment<ChatPrensenter, IChatView>(), IChatView, InputViewController.OnInputActionCallback, BackPressCallback, MessagesAdapter.OnMessageActionListener, InputViewController.RecordActionsCallback, AttachmentsViewBinder.VoiceActionListener, StickersGridView.OnStickerClickedListener, EmojiconTextView.OnHashTagClickListener {

    private var headerView: View? = null
    private var loadMoreFooterHelper: LoadMoreFooterHelper? = null

    private var recyclerView: RecyclerView? = null
    private var adapter: MessagesAdapter? = null

    private var inputViewController: InputViewController? = null
    private var emptyText: TextView? = null

    private var pinnedView: View? = null
    private var pinnedAvatar: ImageView? = null
    private var pinnedTitle: TextView? = null
    private var pinnedSubtitle: TextView? = null

    private val optionMenuSettings = SparseBooleanArray()

    private var toolbarRootView: ViewGroup? = null
    private var actionModeHolder: ActionModeHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false) as ViewGroup
        root.background = CurrentTheme.getChatBackground(activity)

        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        emptyText = root.findViewById(R.id.fragment_chat_empty_text)
        toolbarRootView = root.findViewById(R.id.toolbarRootView)

        recyclerView = root.findViewById(R.id.fragment_friend_dialog_list)
        recyclerView?.run {
            layoutManager = createLayoutManager()
            itemAnimator.changeDuration = 0
            itemAnimator.addDuration = 0
            itemAnimator.moveDuration = 0
            itemAnimator.removeDuration = 0
            addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        }

        headerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false)

        loadMoreFooterHelper = LoadMoreFooterHelper.createFrom(headerView) {
            presenter?.fireLoadUpButtonClick()
        }

        inputViewController = InputViewController(requireActivity(), root, true, this)
                .also {
                    it.setSendOnEnter(Settings.get().main().isSendByEnter)
                    it.setRecordActionsCallback(this)
                    it.setOnSickerClickListener(this)
                }

        pinnedView = root.findViewById(R.id.pinned_root_view)
        pinnedAvatar = pinnedView?.findViewById(R.id.pinned_avatar)
        pinnedTitle = pinnedView?.findViewById(R.id.pinned_title)
        pinnedSubtitle = pinnedView?.findViewById(R.id.pinned_subtitle)
        return root
    }

    private class ActionModeHolder(val rootView: View, fragment: ChatFragment): View.OnClickListener {

        override fun onClick(v: View) {
            when(v.id){
                R.id.buttonClose -> hide()
                R.id.buttonEdit -> {
                    reference.get()?.presenter?.fireActionModeEditClick()
                    hide()
                }
                R.id.buttonForward -> {
                    reference.get()?.presenter?.onActionModeForwardClick()
                    hide()
                }
                R.id.buttonCopy -> {
                    reference.get()?.presenter?.fireActionModeCopyClick()
                    hide()
                }
                R.id.buttonDelete -> {
                    reference.get()?.presenter?.fireActionModeDeleteClick()
                    hide()
                }
            }
        }

        val reference = WeakReference(fragment)
        val buttonClose: View = rootView.findViewById(R.id.buttonClose)
        val buttonEdit: View = rootView.findViewById(R.id.buttonEdit)
        val buttonForward: View = rootView.findViewById(R.id.buttonForward)
        val buttonCopy: View = rootView.findViewById(R.id.buttonCopy)
        val buttonDelete: View = rootView.findViewById(R.id.buttonDelete)
        val titleView: TextView = rootView.findViewById(R.id.actionModeTitle)

        init {
            buttonClose.setOnClickListener(this)
            buttonEdit.setOnClickListener(this)
            buttonForward.setOnClickListener(this)
            buttonCopy.setOnClickListener(this)
            buttonDelete.setOnClickListener(this)
        }

        fun show(){
            rootView.visibility = View.VISIBLE
        }

        fun isVisible(): Boolean = rootView.visibility == View.VISIBLE

        fun hide(){
            rootView.visibility = View.GONE
            reference.get()?.presenter?.fireActionModeDestroy()
        }
    }

    override fun onRecordCancel() {
        presenter?.fireRecordCancelClick()
    }

    override fun onSwithToRecordMode() {
        presenter?.fireRecordingButtonClick()
    }

    override fun onRecordSendClick() {
        presenter?.fireRecordSendClick()
    }

    override fun onResumePauseClick() {
        presenter?.fireRecordResumePauseClick()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_chat, menu)
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
    }

    override fun displayMessages(messages: List<Message>, lastReadId: LastReadId) {
        adapter = MessagesAdapter(activity, messages, lastReadId, this)
                .also {
                    it.setOnMessageActionListener(this)
                    it.setVoiceActionListener(this)
                    it.addFooter(headerView)
                    it.setOnHashTagClickListener(this)
                }

        recyclerView?.adapter = adapter
    }

    override fun notifyMessagesUpAdded(position: Int, count: Int) {
        adapter?.run {
            notifyItemRangeChanged(position + headersCount, count) //+header if exist
        }
    }

    override fun notifyDataChanged() {
        adapter?.notifyDataSetChanged()
    }

    override fun notifyMessagesDownAdded(count: Int) {

    }

    override fun configNowVoiceMessagePlaying(voiceId: Int, progress: Float, paused: Boolean, amin: Boolean) {
        adapter?.configNowVoiceMessagePlaying(voiceId, progress, paused, amin)
    }

    override fun bindVoiceHolderById(holderId: Int, play: Boolean, paused: Boolean, progress: Float, amin: Boolean) {
        adapter?.bindVoiceHolderById(holderId, play, paused, progress, amin)
    }

    override fun disableVoicePlaying() {
        adapter?.disableVoiceMessagePlaying()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<ChatPrensenter> = object : IPresenterFactory<ChatPrensenter> {
        override fun create(): ChatPrensenter {
            val aid = requireArguments().getInt(Extra.ACCOUNT_ID)
            val messagesOwnerId = requireArguments().getInt(Extra.OWNER_ID)
            val peer = requireArguments().getParcelable<Peer>(Extra.PEER)
            return ChatPrensenter(aid, messagesOwnerId, peer, createStartConfig(), saveInstanceState)
        }
    }

    private fun createStartConfig(): ChatConfig {
        val config = ChatConfig()

        config.isCloseOnSend = activity is SendAttachmentsActivity

        val inputStreams = ActivityUtils.checkLocalStreams(requireActivity())
        config.uploadFiles = if (safeIsEmpty(inputStreams)) null else inputStreams

        val models = activity!!.intent.getParcelableExtra<ModelsBundle>(MainActivity.EXTRA_INPUT_ATTACHMENTS)

        models?.run {
            config.appendAll(this)
        }

        val initialText = ActivityUtils.checkLinks(activity!!)
        config.initialText = if (TextUtils.isEmpty(initialText)) null else initialText
        return config
    }

    override fun setupLoadUpHeaderState(@LoadMoreState state: Int) {
        loadMoreFooterHelper?.switchToState(state)
    }

    override fun displayDraftMessageAttachmentsCount(count: Int) {
        inputViewController?.setAttachmentsCount(count)
    }

    override fun displayDraftMessageText(text: String?) {
        inputViewController?.setTextQuietly(text)
    }

    override fun setupSendButton(canSendNormalMessage: Boolean, canSendVoiceMessage: Boolean) {
        inputViewController?.setup(canSendNormalMessage, canSendVoiceMessage)
    }

    override fun displayToolbarTitle(text: String?) {
        ActivityUtils.supportToolbarFor(this)?.title = text
    }

    override fun displayToolbarSubtitle(text: String?) {
        ActivityUtils.supportToolbarFor(this)?.subtitle = text
    }

    override fun setRecordModeActive(active: Boolean) {
        inputViewController?.swithModeTo(if (active) InputViewController.Mode.VOICE_RECORD else InputViewController.Mode.NORMAL)
    }

    override fun requestRecordPermissions() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_RECORD_PERMISSIONS)
    }

    override fun displayRecordingDuration(time: Long) {
        inputViewController?.setRecordingDuration(time)
    }

    override fun doCloseAfterSend() {
        try {
            requireActivity().finish()
        } catch (ignored: Exception) {

        }
    }

    override fun displayPinnedMessage(pinned: Message?) {
        pinnedView?.run {
            visibility = if (pinned == null) View.GONE else View.VISIBLE

            pinned?.run {
                ViewUtils.displayAvatar(pinnedAvatar!!, CurrentTheme.createTransformationForAvatar(requireContext()),
                        sender.get100photoOrSmaller(), null)

                pinnedTitle?.text = this.sender.fullName
                pinnedSubtitle?.text = this.body
            }
        }
    }

    override fun onVoiceHolderBinded(voiceMessageId: Int, voiceHolderId: Int) {
        presenter?.fireVoiceHolderCreated(voiceMessageId, voiceHolderId)
    }

    override fun onVoicePlayButtonClick(voiceHolderId: Int, voiceMessageId: Int, voiceMessage: VoiceMessage) {
        presenter?.fireVoicePlayButtonClick(voiceHolderId, voiceMessageId, voiceMessage)
    }

    override fun onStickerClick(stickerId: Int) {
        presenter?.fireStickerSendClick(stickerId)
    }

    override fun onHashTagClicked(hashTag: String) {
        presenter?.fireHashtagClick(hashTag)
    }

    override fun showActionMode(title: String, canEdit: Boolean) {
        toolbarRootView?.run {
            if(childCount == 1){
                val v = LayoutInflater.from(context).inflate(R.layout.view_actionmode, this, false)
                actionModeHolder = ActionModeHolder(v, this@ChatFragment)
                addView(v)
            }
        }

        actionModeHolder?.show()
        actionModeHolder?.titleView?.text = title
        actionModeHolder?.buttonEdit?.visibility = if(canEdit) View.VISIBLE else View.GONE
    }

    override fun finishActionMode() {
        actionModeHolder?.rootView?.visibility = View.GONE
    }

    private fun onEditLocalPhotosSelected(photos: List<LocalPhoto>){
        val defaultSize = Settings.get().main().uploadImageSize
        when(defaultSize){
            null -> {
                ImageSizeAlertDialog.Builder(activity)
                        .setOnSelectedCallback { size -> presenter?.fireEditLocalPhotosSelected(photos, size) }
                        .show()
            }
            else -> presenter?.fireEditLocalPhotosSelected(photos, defaultSize)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT_MESSAGE) {
            if (data != null && data.hasExtra(Extra.BUNDLE)) {
                val bundle = data.getParcelableExtra<ModelsBundle>(Extra.BUNDLE)
                presenter?.fireEditMessageResult(bundle)
            }

            if (resultCode == Activity.RESULT_OK) {
                presenter?.fireSendClickFromAttachmens()
            }
        }

        if(requestCode == REQUEST_ADD_VKPHOTO && resultCode == Activity.RESULT_OK){
            val vkphotos: List<Photo> = data?.getParcelableArrayListExtra(Extra.ATTACHMENTS) ?: Collections.emptyList()
            val localPhotos: List<LocalPhoto> = data?.getParcelableArrayListExtra(Extra.PHOTOS) ?: Collections.emptyList()

            if(vkphotos.isNotEmpty()){
                presenter?.fireEditPhotosSelected(vkphotos)
            } else if(localPhotos.isNotEmpty()){
                onEditLocalPhotosSelected(localPhotos)
            }
        }
    }

    override fun goToMessageAttachmentsEditor(accountId: Int, messageOwnerId: Int, destination: UploadDestination,
                                              body: String?, attachments: ModelsBundle?) {
        val fragment = MessageAttachmentsFragment.newInstance(accountId, messageOwnerId, destination.id, attachments)
        fragment.setTargetFragment(this, REQUEST_EDIT_MESSAGE)
        fragment.show(requireFragmentManager(), "message-attachments")
    }

    override fun startImagesSelection(accountId: Int, ownerId: Int) {
        val sources = Sources()
                .with(LocalPhotosSelectableSource())
                .with(VkPhotosSelectableSource(accountId, ownerId))

        val intent = DualTabPhotoActivity.createIntent(activity, 10, sources)
        startActivityForResult(intent, REQUEST_ADD_VKPHOTO)
    }

    private class EditAttachmentsHolder(rootView: View, fragment: ChatFragment, attachments: MutableList<AttachmenEntry>) : AttachmentsBottomSheetAdapter.ActionListener, View.OnClickListener {
        override fun onClick(v: View) {
            when(v.id){
                R.id.buttonHide -> reference.get()?.hideEditAttachmentsDialog()
                R.id.buttonSave -> reference.get()?.onEditAttachmentSaveClick()
            }
        }

        override fun onAddPhotoButtonClick() {
            reference.get()?.presenter?.fireEditAddImageClick()
        }

        override fun onButtonRemoveClick(entry: AttachmenEntry) {
            reference.get()?.presenter?.fireEditAttachmentRemoved(entry)
        }

        val reference = WeakReference(fragment)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        val emptyView: View = rootView.findViewById(R.id.emptyRootView)
        val adapter = AttachmentsBottomSheetAdapter(rootView.context, attachments, this)

        init {
            recyclerView.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter

            rootView.findViewById<View>(R.id.buttonHide).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonVideo).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonDoc).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonCamera).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonSave).setOnClickListener(this)

            checkEmptyViewVisibility()
        }

        fun checkEmptyViewVisibility(){
            emptyView.visibility = if(adapter.itemCount < 2) View.VISIBLE else View.INVISIBLE
        }

        fun notifyAttachmentRemoved(index: Int){
            adapter.notifyItemRemoved(index + 1)
            checkEmptyViewVisibility()
        }

        fun notifyAttachmentChanged(index: Int){
            adapter.notifyItemChanged(index + 1)
        }

        fun notifyAttachmentsAdded(position: Int, count: Int){
            adapter.notifyItemRangeInserted(position + 1, count)
            checkEmptyViewVisibility()
        }

        fun notifyAttachmentProgressUpdate(index: Int, progress: Int){
            adapter.changeUploadProgress(index, progress, true)
        }
    }

    override fun notifyEditUploadProgressUpdate(index: Int, progress: Int) {
        editAttachmentsHolder?.notifyAttachmentProgressUpdate(index, progress)
    }

    override fun notifyEditAttachmentsAdded(position: Int, size: Int) {
        editAttachmentsHolder?.notifyAttachmentsAdded(position, size)
    }

    override fun notifyEditAttachmentChanged(index: Int) {
        editAttachmentsHolder?.notifyAttachmentChanged(index)
    }

    override fun notifyEditAttachmentRemoved(index: Int) {
        editAttachmentsHolder?.notifyAttachmentRemoved(index)
    }

    private fun onEditAttachmentSaveClick() {
        hideEditAttachmentsDialog()
        presenter?.fireEditMessageSaveClick()
    }

    private fun hideEditAttachmentsDialog() {
        editAttachmentsDialog?.dismiss()
        editAttachmentsHolder = null
    }

    private var editAttachmentsHolder: EditAttachmentsHolder? = null
    private var editAttachmentsDialog: Dialog? = null

    override fun showEditAttachmentsDialog(attachments: MutableList<AttachmenEntry>) {
        val view = View.inflate(requireActivity(), R.layout.bottom_sheet_attachments_edit, null)

        editAttachmentsHolder = EditAttachmentsHolder(view, this, attachments)
        editAttachmentsDialog = BottomSheetDialog(requireActivity())
                .apply {
                    setContentView(view)
                    show()
                }
    }

    override fun showErrorSendDialog(message: Message) {
        val items = arrayOf(getString(R.string.try_again), getString(R.string.delete))

        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.sending_message_failed)
                .setItems(items) { _, i ->
                    when (i) {
                        0 -> presenter?.fireSendAgainClick(message)
                        1 -> presenter?.fireErrorMessageDeleteClick(message)
                    }
                }
                .setCancelable(true)
                .show()
    }

    override fun notifyItemRemoved(position: Int) {
        adapter?.run {
            notifyItemRemoved(position + headersCount) // +headers count
        }
    }

    override fun configOptionMenu(canLeaveChat: Boolean, canChangeTitle: Boolean, canShowMembers: Boolean,
                                  encryptionStatusVisible: Boolean, encryprionEnabled: Boolean, encryptionPlusEnabled: Boolean, keyExchangeVisible: Boolean) {
        optionMenuSettings.put(LEAVE_CHAT_VISIBLE, canLeaveChat)
        optionMenuSettings.put(CHANGE_CHAT_TITLE_VISIBLE, canChangeTitle)
        optionMenuSettings.put(CHAT_MEMBERS_VISIBLE, canShowMembers)
        optionMenuSettings.put(ENCRYPTION_STATUS_VISIBLE, encryptionStatusVisible)
        optionMenuSettings.put(ENCRYPTION_ENABLED, encryprionEnabled)
        optionMenuSettings.put(ENCRYPTION_PLUS_ENABLED, encryptionPlusEnabled)
        optionMenuSettings.put(KEY_EXCHANGE_VISIBLE, keyExchangeVisible)

        try {
            requireActivity().invalidateOptionsMenu()
        } catch (ignored: Exception) {

        }

    }

    override fun goToSearchMessage(accountId: Int, peer: Peer) {
        val criteria = MessageSeachCriteria("").setPeerId(peer.id)
        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.MESSAGES, criteria).tryOpenWith(requireActivity())
    }

    override fun showImageSizeSelectDialog(streams: List<Uri>) {
        ImageSizeAlertDialog.Builder(activity)
                .setOnSelectedCallback { size -> presenter?.fireImageUploadSizeSelected(streams, size) }
                .setOnCancelCallback { presenter?.fireUploadCancelClick() }
                .show()
    }

    override fun resetUploadImages() {
        ActivityUtils.resetInputPhotos(requireActivity())
    }

    override fun resetInputAttachments() {
        requireActivity().intent.removeExtra(MainActivity.EXTRA_INPUT_ATTACHMENTS)
        ActivityUtils.resetInputText(requireActivity())
    }

    override fun notifyChatResume(accountId: Int, peerId: Int, title: String?, image: String?) {
        if (activity is OnSectionResumeCallback) {
            (activity as OnSectionResumeCallback).onChatResume(accountId, peerId, title, image)
        }
    }

    override fun goToConversationAttachments(accountId: Int, peerId: Int) {
        val types = arrayOf(VKApiAttachment.TYPE_PHOTO, VKApiAttachment.TYPE_VIDEO, VKApiAttachment.TYPE_DOC, VKApiAttachment.TYPE_AUDIO)

        val items = arrayOf(getString(R.string.photos), getString(R.string.videos), getString(R.string.documents), getString(R.string.music))

        AlertDialog.Builder(requireActivity()).setItems(items) { _, which ->
            showConversationAttachments(accountId, peerId, types[which])
        }.show()
    }

    private fun showConversationAttachments(accountId: Int, peerId: Int, type: String) {
        PlaceFactory.getConversationAttachmentsPlace(accountId, peerId, type).tryOpenWith(requireActivity())
    }

    override fun goToChatMembers(accountId: Int, chatId: Int) {
        PlaceFactory.getChatMembersPlace(accountId, chatId).tryOpenWith(requireActivity())
    }

    override fun showChatTitleChangeDialog(initialValue: String?) {
        InputTextDialog.Builder(requireActivity())
                .setAllowEmpty(false)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(initialValue)
                .setTitleRes(R.string.change_chat_title)
                .setCallback { newValue -> presenter?.fireChatTitleTyped(newValue) }
                .show()
    }

    override fun forwardMessagesToAnotherConversation(messages: ArrayList<Message>, accountId: Int) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, FwdMessages(messages))
    }

    override fun diplayForwardTypeSelectDialog(messages: ArrayList<Message>) {
        val items = arrayOf(getString(R.string.here), getString(R.string.to_another_dialogue))

        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> presenter?.fireForwardToHereClick(messages)
                1 -> presenter?.fireForwardToAnotherClick(messages)
            }
        }

        AlertDialog.Builder(requireActivity())
                .setItems(items, listener)
                .setCancelable(true)
                .show()
    }

    override fun setEmptyTextVisible(visible: Boolean) {
        emptyText?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setupRecordPauseButton(available: Boolean, isPlaying: Boolean) {
        inputViewController?.setupRecordPauseButton(available, isPlaying)
    }

    override fun displayIniciateKeyExchangeQuestion(@KeyLocationPolicy keyStoragePolicy: Int) {
        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.key_exchange)
                .setMessage(R.string.you_dont_have_encryption_keys_stored_initiate_key_exchange)
                .setPositiveButton(R.string.button_ok) { _, _ -> presenter?.fireIniciateKeyExchangeClick(keyStoragePolicy) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showEncryptionKeysPolicyChooseDialog(requestCode: Int) {
        val view = View.inflate(activity, R.layout.dialog_select_encryption_key_policy, null)
        val buttonOnDisk = view.findViewById<RadioButton>(R.id.button_on_disk)
        val buttonInRam = view.findViewById<RadioButton>(R.id.button_in_ram)

        buttonOnDisk.isChecked = true

        AlertDialog.Builder(requireActivity())
                .setTitle(R.string.choose_location_key_store)
                .setView(view)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    if (buttonOnDisk.isChecked) {
                        presenter?.fireDiskKeyStoreSelected(requestCode)
                    } else if (buttonInRam.isChecked) {
                        presenter?.fireRamKeyStoreSelected(requestCode)
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showEncryptionDisclaimerDialog(requestCode: Int) {
        val view = View.inflate(activity, R.layout.content_encryption_terms_of_use, null)
        AlertDialog.Builder(requireActivity())
                .setView(view)
                .setTitle(R.string.phoenix_encryption)
                .setPositiveButton(R.string.button_accept) { _, _ -> presenter?.fireTermsOfUseAcceptClick(requestCode) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(activity, true)
                .build()
                .apply(requireActivity())
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.action_leave_chat).isVisible = optionMenuSettings.get(LEAVE_CHAT_VISIBLE, false)
        menu.findItem(R.id.action_change_chat_title).isVisible = optionMenuSettings.get(CHANGE_CHAT_TITLE_VISIBLE, false)
        menu.findItem(R.id.action_chat_members).isVisible = optionMenuSettings.get(CHAT_MEMBERS_VISIBLE, false)
        menu.findItem(R.id.action_key_exchange).isVisible = optionMenuSettings.get(KEY_EXCHANGE_VISIBLE, false)

        val encryptionStatusItem = menu.findItem(R.id.crypt_state)
        val encryptionStatusVisible = optionMenuSettings.get(ENCRYPTION_STATUS_VISIBLE, false)

        encryptionStatusItem.isVisible = encryptionStatusVisible

        if (encryptionStatusVisible) {
            @AttrRes
            var attrRes = R.attr.toolbarUnlockIcon

            if (optionMenuSettings.get(ENCRYPTION_ENABLED, false)) {
                attrRes = if (optionMenuSettings.get(ENCRYPTION_PLUS_ENABLED, false)) {
                    R.attr.toolbarLockPlusIcon
                } else {
                    R.attr.toolbarLockIcon
                }
            }

            try {
                encryptionStatusItem.setIcon(CurrentTheme.getResIdFromAttribute(activity, attrRes))
            } catch (e: Exception) {
                //java.lang.NullPointerException: Attempt to invoke virtual method
                // 'android.content.res.Resources$Theme android.app.Activity.getTheme()' on a null object reference
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                presenter?.fireRefreshClick()
                return true
            }
            R.id.action_leave_chat -> {
                presenter?.fireLeaveChatClick()
                return true
            }
            R.id.action_change_chat_title -> {
                presenter?.fireChatTitleClick()
                return true
            }
            R.id.action_chat_members -> {
                presenter?.fireChatMembersClick()
                return true
            }

            R.id.action_attachments_in_conversation -> presenter?.fireDialogAttachmentsClick()
            R.id.messages_search -> presenter?.fireSearchClick()
            R.id.crypt_state -> presenter?.fireEncriptionStatusClick()
            R.id.action_key_exchange -> presenter?.fireKeyExchangeClick()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_PERMISSIONS) {
            presenter?.fireRecordPermissionsResolved()
        }
    }

    override fun onInputTextChanged(s: String) {
        presenter?.fireDraftMessageTextEdited(s)
    }

    override fun onSendClicked(body: String) {
        presenter?.fireSendClick()
    }

    override fun onAttachClick() {
        presenter?.fireAttachButtonClick()
    }

    override fun onBackPressed(): Boolean {
        if(actionModeHolder?.isVisible() == true){
            actionModeHolder?.hide()
            return false
        }

        if(inputViewController?.onBackPressed() == false){
            return false
        }

        return presenter?.onBackPressed() == true
    }

    fun reInit(newAccountId: Int, newMessagesOwnerId: Int, newPeerId: Int, title: String) {
        presenter?.reInitWithNewPeer(newAccountId, newMessagesOwnerId, newPeerId, title)
    }

    private fun isActionModeVisible(): Boolean {
        return actionModeHolder?.rootView?.visibility == View.VISIBLE
    }

    override fun onAvatarClick(message: Message, userId: Int) {
        if (isActionModeVisible()) {
            presenter?.fireMessageClick(message)
        } else {
            presenter?.fireOwnerClick(userId)
        }
    }

    override fun onRestoreClick(message: Message, position: Int) {
        presenter?.fireMessageRestoreClick(message)
    }

    override fun onMessageLongClick(message: Message): Boolean {
        presenter?.fireMessageLongClick(message)
        return true
    }

    override fun onMessageClicked(message: Message) {
        presenter?.fireMessageClick(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputViewController?.destroyView()
        inputViewController = null
    }

    companion object {

        private const val REQUEST_RECORD_PERMISSIONS = 15
        private const val REQUEST_EDIT_MESSAGE = 150
        private const val REQUEST_ADD_VKPHOTO = 151

        fun buildArgs(accountId: Int, peerId: Int, title: String, avaUrl: String): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            bundle.putInt(Extra.PEER_ID, peerId)
            bundle.putString(Extra.TITLE, title)
            bundle.putString(Extra.IMAGE, avaUrl)
            return bundle
        }

        fun newInstance(accountId: Int, messagesOwnerId: Int, peer: Peer): ChatFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.OWNER_ID, messagesOwnerId)
            args.putParcelable(Extra.PEER, peer)

            val fragment = ChatFragment()
            fragment.arguments = args
            return fragment
        }

        private const val LEAVE_CHAT_VISIBLE = 1
        private const val CHANGE_CHAT_TITLE_VISIBLE = 2
        private const val CHAT_MEMBERS_VISIBLE = 3
        private const val ENCRYPTION_STATUS_VISIBLE = 4
        private const val ENCRYPTION_ENABLED = 5
        private const val ENCRYPTION_PLUS_ENABLED = 6
        private const val KEY_EXCHANGE_VISIBLE = 7
    }
}
package biz.dealnote.messenger.fragment

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
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
import biz.dealnote.messenger.activity.ActivityFeatures
import biz.dealnote.messenger.activity.ActivityUtils
import biz.dealnote.messenger.activity.MainActivity
import biz.dealnote.messenger.activity.SendAttachmentsActivity
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

    private var mHeaderView: View? = null
    private var mLoadMoreFooterHelper: LoadMoreFooterHelper? = null

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MessagesAdapter? = null

    private var mInputViewController: InputViewController? = null
    private var mEmptyText: TextView? = null

    private var mPinnedView: View? = null
    private var mPinnedAvatar: ImageView? = null
    private var mPinnedTitle: TextView? = null
    private var mPinnedSubtitle: TextView? = null

    private var mActionMode: ActionMode? = null
    private val mActionModeCallback = ActionModeCallback(this)

    private val mOptionMenuSettings = SparseBooleanArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false) as ViewGroup
        root.background = CurrentTheme.getChatBackground(activity)

        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        mEmptyText = root.findViewById(R.id.fragment_chat_empty_text)

        mRecyclerView = root.findViewById(R.id.fragment_friend_dialog_list)
        mRecyclerView?.run {
            layoutManager = createLayoutManager()
            itemAnimator.changeDuration = 0
            itemAnimator.addDuration = 0
            itemAnimator.moveDuration = 0
            itemAnimator.removeDuration = 0
            addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        }

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false)

        mLoadMoreFooterHelper = LoadMoreFooterHelper.createFrom(mHeaderView) {
            presenter?.fireLoadUpButtonClick()
        }

        mInputViewController = InputViewController(requireActivity(), root, true, this)
                .also {
                    it.setSendOnEnter(Settings.get().main().isSendByEnter)
                    it.setRecordActionsCallback(this)
                    it.setOnSickerClickListener(this)
                }

        mPinnedView = root.findViewById(R.id.pinned_root_view)
        mPinnedAvatar = mPinnedView?.findViewById(R.id.pinned_avatar)
        mPinnedTitle = mPinnedView?.findViewById(R.id.pinned_title)
        mPinnedSubtitle = mPinnedView?.findViewById(R.id.pinned_subtitle)
        return root
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
        mAdapter = MessagesAdapter(activity, messages, lastReadId, this)
                .also {
                    it.setOnMessageActionListener(this)
                    it.setVoiceActionListener(this)
                    it.addFooter(mHeaderView)
                    it.setOnHashTagClickListener(this)
                }

        mRecyclerView?.adapter = mAdapter
    }

    override fun notifyMessagesUpAdded(position: Int, count: Int) {
        mAdapter?.run {
            notifyItemRangeChanged(position + headersCount, count) //+header if exist
        }
    }

    override fun notifyDataChanged() {
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyMessagesDownAdded(count: Int) {

    }

    override fun configNowVoiceMessagePlaying(voiceId: Int, progress: Float, paused: Boolean, amin: Boolean) {
        mAdapter?.configNowVoiceMessagePlaying(voiceId, progress, paused, amin)
    }

    override fun bindVoiceHolderById(holderId: Int, play: Boolean, paused: Boolean, progress: Float, amin: Boolean) {
        mAdapter?.bindVoiceHolderById(holderId, play, paused, progress, amin)
    }

    override fun disableVoicePlaying() {
        mAdapter?.disableVoiceMessagePlaying()
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
        mLoadMoreFooterHelper?.switchToState(state)
    }

    override fun displayDraftMessageAttachmentsCount(count: Int) {
        mInputViewController?.setAttachmentsCount(count)
    }

    override fun displayDraftMessageText(text: String?) {
        mInputViewController?.setTextQuietly(text)
    }

    override fun setupSendButton(canSendNormalMessage: Boolean, canSendVoiceMessage: Boolean) {
        mInputViewController?.setup(canSendNormalMessage, canSendVoiceMessage)
    }

    override fun displayToolbarTitle(text: String?) {
        ActivityUtils.supportToolbarFor(this)?.title = text
    }

    override fun displayToolbarSubtitle(text: String?) {
        ActivityUtils.supportToolbarFor(this)?.subtitle = text
    }

    override fun setRecordModeActive(active: Boolean) {
        mInputViewController?.swithModeTo(if (active) InputViewController.Mode.VOICE_RECORD else InputViewController.Mode.NORMAL)
    }

    override fun requestRecordPermissions() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_RECORD_PERMISSIONS)
    }

    override fun displayRecordingDuration(time: Long) {
        mInputViewController?.setRecordingDuration(time)
    }

    override fun doCloseAfterSend() {
        try {
            requireActivity().finish()
        } catch (ignored: Exception) {

        }

    }

    override fun displayPinnedMessage(pinned: Message?) {
        mPinnedView?.run {
            visibility = if (pinned == null) View.GONE else View.VISIBLE

            pinned?.run {
                ViewUtils.displayAvatar(mPinnedAvatar!!, CurrentTheme.createTransformationForAvatar(requireContext()),
                        sender.get100photoOrSmaller(), null)

                mPinnedTitle?.text = this.sender.fullName
                mPinnedSubtitle?.text = this.body
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

    private class ActionModeCallback internal constructor(fragment: ChatFragment) : ActionMode.Callback {

        var fragmentWeakReference: WeakReference<ChatFragment> = WeakReference(fragment)

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val fragment = fragmentWeakReference.get() ?: return true

            when (item.itemId) {
                R.id.delete -> fragment.presenter?.fireActionModeDeleteClick()
                R.id.copy -> fragment.presenter?.fireActionModeCopyClick()
                R.id.forward -> fragment.presenter?.fireForwardClick()
            }

            mode.finish()
            return true
        }

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.messages_menu, menu)
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            fragmentWeakReference.get()?.run {
                presenter?.fireActionModeDestroy()
                mActionMode = null
            }
        }
    }

    override fun showActionMode(title: String) {
        if (mActionMode == null) {
            mActionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(mActionModeCallback)
        }

        mActionMode?.run {
            this.title = title
            invalidate()
        }
    }

    override fun finishActionMode() {
        mActionMode?.finish()
        mActionMode = null
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
    }

    override fun goToMessageAttachmentsEditor(accountId: Int, messageOwnerId: Int, destination: UploadDestination,
                                              body: String?, attachments: ModelsBundle?) {
        val fragment = MessageAttachmentsFragment.newInstance(accountId, messageOwnerId, destination.id, attachments)
        fragment.setTargetFragment(this, REQUEST_EDIT_MESSAGE)
        fragment.show(requireFragmentManager(), "message-attachments")
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
        mAdapter?.run {
            notifyItemRemoved(position + headersCount) // +headers count
        }
    }

    override fun configOptionMenu(canLeaveChat: Boolean, canChangeTitle: Boolean, canShowMembers: Boolean,
                                  encryptionStatusVisible: Boolean, encryprionEnabled: Boolean, encryptionPlusEnabled: Boolean, keyExchangeVisible: Boolean) {
        mOptionMenuSettings.put(LEAVE_CHAT_VISIBLE, canLeaveChat)
        mOptionMenuSettings.put(CHANGE_CHAT_TITLE_VISIBLE, canChangeTitle)
        mOptionMenuSettings.put(CHAT_MEMBERS_VISIBLE, canShowMembers)
        mOptionMenuSettings.put(ENCRYPTION_STATUS_VISIBLE, encryptionStatusVisible)
        mOptionMenuSettings.put(ENCRYPTION_ENABLED, encryprionEnabled)
        mOptionMenuSettings.put(ENCRYPTION_PLUS_ENABLED, encryptionPlusEnabled)
        mOptionMenuSettings.put(KEY_EXCHANGE_VISIBLE, keyExchangeVisible)

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
        mEmptyText?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setupRecordPauseButton(available: Boolean, isPlaying: Boolean) {
        mInputViewController?.setupRecordPauseButton(available, isPlaying)
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

        menu.findItem(R.id.action_leave_chat).isVisible = mOptionMenuSettings.get(LEAVE_CHAT_VISIBLE, false)
        menu.findItem(R.id.action_change_chat_title).isVisible = mOptionMenuSettings.get(CHANGE_CHAT_TITLE_VISIBLE, false)
        menu.findItem(R.id.action_chat_members).isVisible = mOptionMenuSettings.get(CHAT_MEMBERS_VISIBLE, false)
        menu.findItem(R.id.action_key_exchange).isVisible = mOptionMenuSettings.get(KEY_EXCHANGE_VISIBLE, false)

        val encryptionStatusItem = menu.findItem(R.id.crypt_state)
        val encryptionStatusVisible = mOptionMenuSettings.get(ENCRYPTION_STATUS_VISIBLE, false)

        encryptionStatusItem.isVisible = encryptionStatusVisible

        if (encryptionStatusVisible) {
            @AttrRes
            var attrRes = R.attr.toolbarUnlockIcon

            if (mOptionMenuSettings.get(ENCRYPTION_ENABLED, false)) {
                attrRes = if (mOptionMenuSettings.get(ENCRYPTION_PLUS_ENABLED, false)) {
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
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
        return mInputViewController == null || mInputViewController!!.onBackPressed()
    }

    fun reInit(newAccountId: Int, newMessagesOwnerId: Int, newPeerId: Int, title: String) {
        presenter?.reInitWithNewPeer(newAccountId, newMessagesOwnerId, newPeerId, title)
    }

    override fun onAvatarClick(message: Message, userId: Int) {
        if (mActionMode != null) {
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
        mInputViewController?.destroyView()
        mInputViewController = null
    }

    companion object {

        private const val REQUEST_RECORD_PERMISSIONS = 15
        private const val REQUEST_EDIT_MESSAGE = 150

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
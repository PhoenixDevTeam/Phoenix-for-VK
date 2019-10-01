package biz.dealnote.messenger.mvp.view

import android.net.Uri
import biz.dealnote.messenger.crypt.KeyLocationPolicy
import biz.dealnote.messenger.model.*
import biz.dealnote.messenger.upload.UploadDestination

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
interface IChatView : IBasicMessageListView, IErrorView {

    fun setupLoadUpHeaderState(@LoadMoreState state: Int)
    fun displayDraftMessageAttachmentsCount(count: Int)
    fun displayDraftMessageText(text: String?)
    fun displayToolbarTitle(text: String?)
    fun displayToolbarSubtitle(text: String?)
    fun requestRecordPermissions()
    fun displayRecordingDuration(time: Long)
    fun doCloseAfterSend()

    fun setupPrimaryButtonAsEditing(canSave: Boolean)
    fun setupPrimaryButtonAsRecording()
    fun setupPrimaryButtonAsRegular(canSend: Boolean, canStartRecoring: Boolean)

    fun displayPinnedMessage(pinned: Message?, canChange: Boolean)
    fun hideInputView()

    fun goToMessageAttachmentsEditor(accountId: Int, messageOwnerId: Int, destination: UploadDestination,
                                     body: String?, attachments: ModelsBundle?)

    fun showErrorSendDialog(message: Message)
    fun notifyItemRemoved(position: Int)

    fun configOptionMenu(canLeaveChat: Boolean, canChangeTitle: Boolean, canShowMembers: Boolean,
                         encryptionStatusVisible: Boolean, encryprionEnabled: Boolean, encryptionPlusEnabled: Boolean, keyExchangeVisible: Boolean)

    fun goToSearchMessage(accountId: Int, peer: Peer)
    fun showImageSizeSelectDialog(streams: List<Uri>)

    fun resetUploadImages()
    fun resetInputAttachments()
    fun notifyChatResume(accountId: Int, peerId: Int, title: String?, image: String?)
    fun goToConversationAttachments(accountId: Int, peerId: Int)
    fun goToChatMembers(accountId: Int, chatId: Int)
    fun showChatTitleChangeDialog(initialValue: String?)
    fun forwardMessagesToAnotherConversation(messages: ArrayList<Message>, accountId: Int)
    fun diplayForwardTypeSelectDialog(messages: ArrayList<Message>)
    fun setEmptyTextVisible(visible: Boolean)
    fun setupRecordPauseButton(available: Boolean, isPlaying: Boolean)
    fun displayIniciateKeyExchangeQuestion(@KeyLocationPolicy keyStoragePolicy: Int)
    fun showEncryptionKeysPolicyChooseDialog(requestCode: Int)
    fun showEncryptionDisclaimerDialog(requestCode: Int)
    fun showEditAttachmentsDialog(attachments: MutableList<AttachmenEntry>)

    fun displayEditingMessage(message: Message?)

    fun notifyEditAttachmentChanged(index: Int)
    fun notifyEditAttachmentRemoved(index: Int)
    fun startImagesSelection(accountId: Int, ownerId: Int)
    fun notifyEditAttachmentsAdded(position: Int, size: Int)
    fun notifyEditUploadProgressUpdate(index: Int, progress: Int)
    fun startVideoSelection(accountId: Int, ownerId: Int)
    fun startDocSelection(accountId: Int, ownerId: Int)
    fun startCamera(fileUri: Uri)
    fun showDeleteForAllDialog(ids: ArrayList<Int>)
}
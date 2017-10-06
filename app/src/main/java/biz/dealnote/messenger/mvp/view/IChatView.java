package biz.dealnote.messenger.mvp.view;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.upload.UploadDestination;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */
public interface IChatView extends IBasicMessageListView, IErrorView {

    void setupLoadUpHeaderState(@LoadMoreState int state);
    void displayDraftMessageAttachmentsCount(int count);
    void displayDraftMessageText(String text);
    void setupSendButton(boolean canSendNormalMessage, boolean canSendVoiceMessage);
    void displayToolbarTitle(String text);
    void displayToolbarSubtitle(String text);
    void setRecordModeActive(boolean active);
    void requestRecordPermissions();
    void displayRecordingDuration(long time);
    void doCloseAfterSend();

    void goToMessageAttachmentsEditor(int accountId, int messageOwnerId, @NonNull UploadDestination destination,
                                      String body, @Nullable ModelsBundle attachments);
    void showErrorSendDialog(@NonNull Message message);
    void notifyItemRemoved(int position);

    void configOptionMenu(boolean canLeaveChat, boolean canChangeTitle, boolean canShowMembers,
                          boolean encryptionStatusVisible, boolean encryprionEnabled, boolean encryptionPlusEnabled, boolean keyExchangeVisible);
    void goToSearchMessage(int accountId, @NonNull Peer peer);
    void showImageSizeSelectDialog(@NonNull List<Uri> streams);

    void resetUploadImages();
    void resetInputAttachments();
    void notifyChatResume(int accountId, int peerId, String title, String image);
    void goToConversationAttachments(int accountId, int peerId);
    void goToChatMembers(int accountId, int chatId);
    void showChatTitleChangeDialog(String initialValue);
    void forwardMessagesToAnotherConversation(@NonNull ArrayList<Message> messages, int accountId);
    void diplayForwardTypeSelectDialog(@NonNull ArrayList<Message> messages);
    void setEmptyTextVisible(boolean visible);
    void setupRecordPauseButton(boolean avilable, boolean isPlaying);
    void displayIniciateKeyExchangeQuestion(@KeyLocationPolicy int keyStoragePolicy);
    void showEncryptionKeysPolicyChooseDialog(int requestCode);
    void showEncryptionDisclaimerDialog(int requestCode);
}

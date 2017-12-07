package biz.dealnote.messenger.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Map;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ParcelUtils;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class Message extends AbsModel implements Parcelable, Identificable, ISelectable {

    public static Creator<Message> CREATOR = new Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    private int accountId;

    private int id;

    private String body;

    private String title;

    private int peerId;

    private int senderId;

    private boolean read;

    private boolean out;

    @MessageStatus
    private int status;

    private long date;

    private boolean selected;

    private boolean deleted;

    private int originalId;

    private boolean important;

    private Attachments attachments;

    private ArrayList<Message> fwd;

    //chat_columns
    private String chatActive;

    //private String pushSettings;

    private int usersCount;

    private int adminId;

    @ChatAction
    private int action;

    private int actionMid;

    private String actionEmail;

    private String actionText;

    private String photo50;

    private String photo100;

    private String photo200;

    private User actionUser;

    private int randomId;

    private Map<Integer, String> extras;

    @CryptStatus
    private int cryptStatus;

    private String decryptedBody;

    private Owner sender;

    private int forwardMessagesCount;

    private boolean hasAttachments;

    public Message(int id) {
        this.id = id;
    }

    public Message(Parcel in) {
        super(in);
        this.accountId = in.readInt();
        this.id = in.readInt();
        this.body = in.readString();
        this.decryptedBody = in.readString();
        this.title = in.readString();
        this.peerId = in.readInt();
        this.senderId = in.readInt();
        this.read = in.readInt() == 1;
        this.out = in.readInt() == 1;
        this.important = in.readInt() == 1;

        @MessageStatus
        int tStatus = in.readInt();
        this.status = tStatus;

        @CryptStatus
        int cs = in.readInt();
        this.cryptStatus = cs;

        this.date = in.readLong();
        this.selected = in.readInt() == 1;
        this.deleted = in.readInt() == 1;
        this.attachments = in.readParcelable(Attachments.class.getClassLoader());
        this.fwd = in.createTypedArrayList(Message.CREATOR);
        this.originalId = in.readInt();
        this.chatActive = in.readString();
        //this.pushSettings = in.readString();
        this.usersCount = in.readInt();
        this.adminId = in.readInt();

        @ChatAction
        int tmpChatAction = in.readInt();
        this.action = tmpChatAction;

        this.actionMid = in.readInt();
        this.actionEmail = in.readString();
        this.actionText = in.readString();
        this.photo50 = in.readString();
        this.photo100 = in.readString();
        this.photo200 = in.readString();
        this.actionUser = in.readParcelable(User.class.getClassLoader());
        this.sender = in.readParcelable(senderId > 0 ?
                User.class.getClassLoader() : Community.class.getClassLoader());
        this.randomId = in.readInt();
        this.extras = ParcelUtils.readIntStringMap(in);

        this.forwardMessagesCount = in.readInt();
        this.hasAttachments = in.readInt() == 1;
    }

    public Message setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
        return this;
    }

    public boolean isHasAttachments() {
        return hasAttachments;
    }

    public Message setForwardMessagesCount(int forwardMessagesCount) {
        this.forwardMessagesCount = forwardMessagesCount;
        return this;
    }

    public int getForwardMessagesCount() {
        return forwardMessagesCount;
    }

    @ChatAction
    public static int fromApiChatAction(String action) {
        if (Objects.isNull(action) || action.length() == 0) {
            return ChatAction.NO_ACTION;
        }

        if ("chat_photo_update".equalsIgnoreCase(action)) {
            return ChatAction.PHOTO_UPDATE;
        } else if ("chat_photo_remove".equalsIgnoreCase(action)) {
            return ChatAction.PHOTO_REMOVE;
        } else if ("chat_create".equalsIgnoreCase(action)) {
            return ChatAction.CREATE;
        } else if ("chat_title_update".equalsIgnoreCase(action)) {
            return ChatAction.TITLE_UPDATE;
        } else if ("chat_invite_user".equalsIgnoreCase(action)) {
            return ChatAction.INVITE_USER;
        } else if ("chat_kick_user".equalsIgnoreCase(action)) {
            return ChatAction.KICK_USER;
        } else {
            return ChatAction.NO_ACTION;
        }
    }

    public String getBody() {
        return body;
    }

    public Message setBody(String body) {
        this.body = body;
        return this;
    }

    public int getRandomId() {
        return randomId;
    }

    public Message setRandomId(int randomId) {
        this.randomId = randomId;
        return this;
    }

    public Owner getSender() {
        return sender;
    }

    public Message setSender(Owner sender) {
        this.sender = sender;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public Message setRead(boolean read) {
        this.read = read;
        return this;
    }

    public Message setActionUser(User actionUser) {
        this.actionUser = actionUser;
        return this;
    }

    public boolean isOut() {
        return out;
    }

    public Message setOut(boolean out) {
        this.out = out;
        return this;
    }

    @MessageStatus
    public int getStatus() {
        return status;
    }

    public Message setStatus(@MessageStatus int status) {
        this.status = status;
        return this;
    }

    public boolean isEditing() {
        return status == MessageStatus.EDITING;
    }

    public long getDate() {
        return date;
    }

    public Message setDate(long date) {
        this.date = date;
        return this;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public Message setAttachments(Attachments attachments) {
        this.attachments = attachments;
        return this;
    }

    @Override
    public int getId() {
        return id;
    }

    public Message setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Message setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getPeerId() {
        return peerId;
    }

    public Message setPeerId(int peerId) {
        this.peerId = peerId;
        return this;
    }

    public ArrayList<Message> getFwd() {
        return fwd;
    }

    public Message setFwd(ArrayList<Message> fwd) {
        this.fwd = fwd;
        return this;
    }

    public ArrayList<Message> prepareFwd(int capacity) {
        if (fwd == null) {
            fwd = new ArrayList<>(capacity);
        }

        return fwd;
    }

    public int getOriginalId() {
        return originalId;
    }

    public Message setOriginalId(int originalId) {
        this.originalId = originalId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Message setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isImportant() {
        return important;
    }

    public Message setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public String getChatActive() {
        return chatActive;
    }

    public Message setChatActive(String chatActive) {
        this.chatActive = chatActive;
        return this;
    }

    /*public String getPushSettings() {
        return pushSettings;
    }

    public Message setPushSettings(String pushSettings) {
        this.pushSettings = pushSettings;
        return this;
    }*/

    public int getUsersCount() {
        return usersCount;
    }

    public Message setUsersCount(int usersCount) {
        this.usersCount = usersCount;
        return this;
    }

    public int getAdminId() {
        return adminId;
    }

    public Message setAdminId(int adminId) {
        this.adminId = adminId;
        return this;
    }

    @ChatAction
    public int getAction() {
        return action;
    }

    public Message setAction(@ChatAction int action) {
        this.action = action;
        return this;
    }

    public int getActionMid() {
        return actionMid;
    }

    public Message setActionMid(int actionMid) {
        this.actionMid = actionMid;
        return this;
    }

    public String getActionEmail() {
        return actionEmail;
    }

    public Message setActionEmail(String actionEmail) {
        this.actionEmail = actionEmail;
        return this;
    }

    public String getActionText() {
        return actionText;
    }

    public Message setActionText(String actionText) {
        this.actionText = actionText;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Message setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Message setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Message setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public boolean isServiseMessage() {
        return action != ChatAction.NO_ACTION;
    }

    public int getSenderId() {
        return senderId;
    }

    public Message setSenderId(int senderId) {
        this.senderId = senderId;
        return this;
    }

    public String getServiceText(Context context) {
        String actionSubject = TextUtils.isEmpty(actionEmail) ?
                (Objects.isNull(actionUser) ? null : actionUser.getFullName()) : actionEmail;

        boolean itself = sender.getOwnerId() == actionMid;

        String result = null;

        switch (action) {
            case ChatAction.PHOTO_UPDATE:
                result = context.getString(R.string.service_update_chat_photo, sender.getFullName());
                break;
            case ChatAction.PHOTO_REMOVE:
                result = context.getString(R.string.service_remove_chat_photo, sender.getFullName());
                break;
            case ChatAction.CREATE:
                result = context.getString(R.string.service_create_chat, sender.getFullName(), actionText);
                break;
            case ChatAction.TITLE_UPDATE:
                result = context.getString(R.string.service_changed_chat_name, sender.getFullName(), actionText);
                break;
            case ChatAction.INVITE_USER:
                if (itself) {
                    result = context.getString(R.string.service_return_to_chat, sender.getFullName());
                } else {
                    result = context.getString(R.string.service_invited, sender.getFullName(), actionSubject);
                }
                break;
            case ChatAction.KICK_USER:
                if (itself) {
                    result = context.getString(R.string.service_left_this_chat, sender.getFullName());
                } else {
                    result = context.getString(R.string.service_removed, sender.getFullName(), actionSubject);
                }

                break;
            case ChatAction.NO_ACTION:

                break;
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(accountId);
        dest.writeInt(id);
        dest.writeString(body);
        dest.writeString(decryptedBody);
        dest.writeString(title);
        dest.writeInt(peerId);
        dest.writeInt(senderId);
        dest.writeInt(read ? 1 : 0);
        dest.writeInt(out ? 1 : 0);
        dest.writeInt(important ? 1 : 0);
        dest.writeInt(status);
        dest.writeInt(cryptStatus);
        dest.writeLong(date);
        dest.writeInt(selected ? 1 : 0);
        dest.writeInt(deleted ? 1 : 0);
        dest.writeParcelable(attachments, flags);
        dest.writeTypedList(fwd);
        dest.writeInt(originalId);
        dest.writeString(chatActive);
        //dest.writeString(pushSettings);
        dest.writeInt(usersCount);
        dest.writeInt(adminId);
        dest.writeInt(action);
        dest.writeInt(actionMid);
        dest.writeString(actionEmail);
        dest.writeString(actionText);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeParcelable(actionUser, flags);
        dest.writeParcelable(sender, flags);
        dest.writeInt(randomId);
        ParcelUtils.writeIntStringMap(dest, extras);
        dest.writeInt(forwardMessagesCount);
        dest.writeInt(hasAttachments ? 1 : 0);
    }

    public int getAccountId() {
        return accountId;
    }

    public Message setAccountId(int accountId) {
        this.accountId = accountId;
        return this;
    }

    public boolean isSent() {
        return status == MessageStatus.SENT;
    }

    public boolean isSticker() {
        return nonNull(attachments) && safeCountOf(attachments.getStickers()) > 0;
    }

    public Map<Integer, String> getExtras() {
        return extras;
    }

    public boolean isChatTitleUpdate() {
        return action == ChatAction.TITLE_UPDATE;
    }

    @CryptStatus
    public int getCryptStatus() {
        return cryptStatus;
    }

    public Message setCryptStatus(@CryptStatus int cryptStatus) {
        this.cryptStatus = cryptStatus;
        return this;
    }

    public String getDecryptedBody() {
        return decryptedBody;
    }

    public Message setDecryptedBody(String decryptedBody) {
        this.decryptedBody = decryptedBody;
        return this;
    }

    public static final class Extra {
        public static final int VOICE_RECORD = 1;
    }

    public Message setExtras(Map<Integer, String> extras) {
        this.extras = extras;
        return this;
    }
}
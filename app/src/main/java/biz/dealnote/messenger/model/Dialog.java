package biz.dealnote.messenger.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.util.Objects;

import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.isEmpty;

/**
 * Created by hp-dv6 on 04.06.2016 with Core i7 2670QM.
 * VKMessenger
 */
public class Dialog implements Identificable, Parcelable {

    private int peerId;

    private String title;

    private int unreadCount;

    private String photo50;

    private String photo100;

    private String photo200;

    private Message message;

    private int adminId;

    private Owner interlocutor;

    private int lastMessageId;

    public Dialog() {

    }

    protected Dialog(Parcel in) {
        this.peerId = in.readInt();
        this.title = in.readString();
        this.unreadCount = in.readInt();
        this.photo50 = in.readString();
        this.photo100 = in.readString();
        this.photo200 = in.readString();
        this.message = in.readParcelable(Message.class.getClassLoader());
        this.adminId = in.readInt();

        boolean interlocutorIsNull = in.readInt() == 1;
        if (!interlocutorIsNull) {
            int ownerType = in.readInt();
            this.interlocutor = in.readParcelable(ownerType == OwnerType.COMMUNITY
                    ? Community.class.getClassLoader() : User.class.getClassLoader());
        }

        this.lastMessageId = in.readInt();
    }

    public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
        @Override
        public Dialog createFromParcel(Parcel in) {
            return new Dialog(in);
        }

        @Override
        public Dialog[] newArray(int size) {
            return new Dialog[size];
        }
    };

    public int getPeerId() {
        return peerId;
    }

    public Dialog setPeerId(int peerId) {
        this.peerId = peerId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Dialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public Dialog setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Dialog setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Dialog setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Dialog setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public boolean isChat() {
        return Peer.isGroupChat(peerId);
    }

    public boolean isUser() {
        return Peer.isUser(peerId);
    }

    public boolean isGroup() {
        return Peer.isGroup(peerId);
    }

    public Message getMessage() {
        return message;
    }

    public Dialog setMessage(Message message) {
        this.message = message;
        return this;
    }

    public int getAdminId() {
        return adminId;
    }

    public Dialog setAdminId(int adminId) {
        this.adminId = adminId;
        return this;
    }

    public Owner getInterlocutor() {
        return interlocutor;
    }

    public Dialog setInterlocutor(Owner interlocutor) {
        this.interlocutor = interlocutor;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public Dialog setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public boolean isLastMessageRead() {
        return message != null && message.isRead();
    }

    public boolean isLastMessageOut() {
        return message != null && message.isOut();
    }

    @ChatAction
    public Integer getLastMessageAction() {
        return message == null ? null : message.getAction();
    }

    public boolean hasForwardMessages() {
        return getForwardMessagesCount() > 0;
    }

    public int getForwardMessagesCount() {
        return message == null ? 0 : message.getForwardMessagesCount();
    }

    public boolean hasAttachments() {
        return message != null && message.isHasAttachments();
    }

    public String getLastMessageBody() {
        return Objects.isNull(message) ? "..." : message.getCryptStatus() == CryptStatus.DECRYPTED ? message.getDecryptedBody() : message.getBody();
    }

    @NonNull
    public String getSenderShortName(@NonNull Context context) {
        String targerText = null;
        if (interlocutor instanceof User) {
            targerText = ((User) interlocutor).getFirstName();
        } else if (interlocutor instanceof Community) {
            targerText = ((Community) interlocutor).getName();
        }

        return targerText == null ? context.getString(R.string.unknown_first_name) : targerText;
    }

    public String getDisplayTitle(@NonNull Context context) {
        switch (Peer.getType(peerId)) {
            case Peer.USER:
            case Peer.GROUP:
                return interlocutor == null ? context.getString(R.string.unknown_first_name) + " " + context.getString(R.string.unknown_last_name) : interlocutor.getFullName();
            case Peer.CHAT:
                return title;
            default:
                throw new IllegalStateException("Unknown peer id: " + peerId);
        }
    }

    public long getLastMessageDate() {
        return message == null ? 0 : message.getDate();
    }

    public String getImageUrl() {
        switch (Peer.getType(peerId)) {
            default:
                return interlocutor == null ? null : interlocutor.get100photoOrSmaller();
            case Peer.CHAT:
                String img = firstNonEmptyString(photo100, photo50);

                if (isEmpty(img) && interlocutor != null) {
                    img = interlocutor.get100photoOrSmaller();
                }

                return img;

        }
    }

    @Override
    public int getId() {
        return peerId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(peerId);
        dest.writeString(title);
        dest.writeInt(unreadCount);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeParcelable(message, flags);
        dest.writeInt(adminId);

        dest.writeInt(interlocutor == null ? 1 : 0);
        if (interlocutor != null) {
            dest.writeInt(interlocutor.getOwnerType());
            dest.writeParcelable(interlocutor, flags);
        }

        dest.writeInt(lastMessageId);
    }
}
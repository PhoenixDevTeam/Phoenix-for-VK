package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.api.model.Identificable;

public final class AppChatUser implements Parcelable, Identificable {

    private Owner inviter;

    private final Owner member;

    private final int invitedBy;

    private final String type;

    private boolean canRemove;

    public AppChatUser(Owner member, int invitedBy, String type) {
        this.member = member;
        this.invitedBy = invitedBy;
        this.type = type;
    }

    private AppChatUser(Parcel in) {
        inviter = in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
        member = in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
        invitedBy = in.readInt();
        type = in.readString();
        canRemove = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(new ParcelableOwnerWrapper(inviter), flags);
        dest.writeParcelable(new ParcelableOwnerWrapper(member), flags);
        dest.writeInt(invitedBy);
        dest.writeString(type);
        dest.writeByte((byte) (canRemove ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppChatUser> CREATOR = new Creator<AppChatUser>() {
        @Override
        public AppChatUser createFromParcel(Parcel in) {
            return new AppChatUser(in);
        }

        @Override
        public AppChatUser[] newArray(int size) {
            return new AppChatUser[size];
        }
    };

    public AppChatUser setInviter(Owner inviter) {
        this.inviter = inviter;
        return this;
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public AppChatUser setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
        return this;
    }

    public Owner getInviter() {
        return inviter;
    }

    public int getInvitedBy() {
        return invitedBy;
    }

    public String getType() {
        return type;
    }

    public Owner getMember() {
        return member;
    }

    @Override
    public int getId() {
        return member.getOwnerId();
    }
}
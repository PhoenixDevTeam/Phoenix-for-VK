package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.api.model.Identificable;

public final class AppChatUser implements Parcelable, Identificable {

    private User invited;

    private final User user;

    private final int invitedBy;

    private final String type;

    private boolean canRemove;

    public AppChatUser(User user, int invitedBy, String type) {
        this.user = user;
        this.invitedBy = invitedBy;
        this.type = type;
    }

    protected AppChatUser(Parcel in) {
        invited = in.readParcelable(User.class.getClassLoader());
        user = in.readParcelable(User.class.getClassLoader());
        invitedBy = in.readInt();
        type = in.readString();
        canRemove = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(invited, flags);
        dest.writeParcelable(user, flags);
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

    public AppChatUser setInvited(User invited) {
        this.invited = invited;
        return this;
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public AppChatUser setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
        return this;
    }

    public User getInvited() {
        return invited;
    }

    public int getInvitedBy() {
        return invitedBy;
    }

    public String getType() {
        return type;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int getId() {
        return user.getId();
    }
}
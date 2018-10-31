package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for [follow, friend_accepted, likes]
 */
public final class UsersFeedback extends Feedback implements Parcelable {

    private List<Owner> owners;

    public UsersFeedback(@FeedbackType int type) {
        super(type);
    }

    private UsersFeedback(Parcel in) {
        super(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UsersFeedback> CREATOR = new Creator<UsersFeedback>() {
        @Override
        public UsersFeedback createFromParcel(Parcel in) {
            return new UsersFeedback(in);
        }

        @Override
        public UsersFeedback[] newArray(int size) {
            return new UsersFeedback[size];
        }
    };

    public UsersFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }

    public List<Owner> getOwners() {
        return owners;
    }
}
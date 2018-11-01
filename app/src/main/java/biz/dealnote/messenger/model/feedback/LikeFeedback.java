package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableModelWrapper;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public final class LikeFeedback extends Feedback implements Parcelable  {

    private AbsModel liked;
    private List<Owner> owners;

    // one of FeedbackType.LIKE_PHOTO, FeedbackType.LIKE_POST, FeedbackType.LIKE_VIDEO
    public LikeFeedback(@FeedbackType int type) {
        super(type);
    }

    private LikeFeedback(Parcel in) {
        super(in);
        liked = ParcelableModelWrapper.readModel(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, liked);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LikeFeedback> CREATOR = new Creator<LikeFeedback>() {
        @Override
        public LikeFeedback createFromParcel(Parcel in) {
            return new LikeFeedback(in);
        }

        @Override
        public LikeFeedback[] newArray(int size) {
            return new LikeFeedback[size];
        }
    };

    public List<Owner> getOwners() {
        return owners;
    }

    public AbsModel getLiked() {
        return liked;
    }

    public LikeFeedback setLiked(AbsModel liked) {
        this.liked = liked;
        return this;
    }

    public LikeFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}
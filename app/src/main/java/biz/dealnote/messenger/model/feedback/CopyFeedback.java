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
 * base class for types [copy_post, copy_photo, copy_video]
 */
public final class CopyFeedback extends Feedback implements Parcelable {

    private AbsModel what;
    private List<Owner> owners;

    public CopyFeedback(@FeedbackType int type) {
        super(type);
    }

    private CopyFeedback(Parcel in) {
        super(in);
        what = ParcelableModelWrapper.readModel(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, what);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CopyFeedback> CREATOR = new Creator<CopyFeedback>() {
        @Override
        public CopyFeedback createFromParcel(Parcel in) {
            return new CopyFeedback(in);
        }

        @Override
        public CopyFeedback[] newArray(int size) {
            return new CopyFeedback[size];
        }
    };

    public AbsModel getWhat() {
        return what;
    }

    public CopyFeedback setWhat(AbsModel what) {
        this.what = what;
        return this;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public CopyFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}
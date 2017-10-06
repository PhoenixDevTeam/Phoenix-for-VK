package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by admin on 24.01.2017.
 * phoenix
 */
public final class ParcelableOwnerWrapper implements Parcelable {

    private final int type;
    private final boolean isNull;
    private final Owner owner;

    public ParcelableOwnerWrapper(Owner owner) {
        this.owner = owner;
        this.type = owner == null ? 0 : owner.getOwnerType();
        this.isNull = owner == null;
    }

    public static ParcelableOwnerWrapper wrap(Owner owner){
        return new ParcelableOwnerWrapper(owner);
    }

    public Owner get() {
        return owner;
    }

    private ParcelableOwnerWrapper(Parcel in) {
        type = in.readInt();
        isNull = in.readByte() != 0;

        if(!isNull){
            owner = in.readParcelable(type == OwnerType.USER
                    ? User.class.getClassLoader() : Community.class.getClassLoader());
        } else {
            owner = null;
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeByte((byte) (isNull ? 1 : 0));

        if(!isNull){
            dest.writeParcelable(owner, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcelableOwnerWrapper> CREATOR = new Creator<ParcelableOwnerWrapper>() {
        @Override
        public ParcelableOwnerWrapper createFromParcel(Parcel in) {
            return new ParcelableOwnerWrapper(in);
        }

        @Override
        public ParcelableOwnerWrapper[] newArray(int size) {
            return new ParcelableOwnerWrapper[size];
        }
    };
}
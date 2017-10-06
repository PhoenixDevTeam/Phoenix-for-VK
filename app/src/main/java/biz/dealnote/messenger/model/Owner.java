package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
public abstract class Owner extends AbsModel implements Parcelable {

    @OwnerType
    private final int ownerType;

    protected Owner(int ownerType) {
        this.ownerType = ownerType;
    }

    public Owner(Parcel in) {
        super(in);
        //noinspection ResourceType
        ownerType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(ownerType);
    }

    @OwnerType
    public int getOwnerType() {
        return ownerType;
    }

    public int getOwnerId(){
        throw new UnsupportedOperationException();
    }

    public String getMaxSquareAvatar(){
        throw new UnsupportedOperationException();
    }

    public String get100photoOrSmaller(){
        throw new UnsupportedOperationException();
    }

    public String getFullName(){
        throw new UnsupportedOperationException();
    }
}
package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

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

    public static Owner readOwner(Parcel in){
        return in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
    }

    public static void writeOwner(Parcel dest, int flags, Owner owner){
        dest.writeParcelable(new ParcelableOwnerWrapper(owner), flags);
    }

    public static List<Owner> readOwners(Parcel in){
        boolean isNull = in.readInt() == 1;
        if(isNull){
            return null;
        }

        int ownersCount = in.readInt();
        List<Owner> owners = new ArrayList<>(ownersCount);
        for(int i = 0; i < ownersCount; i++){
            owners.add(readOwner(in));
        }

        return owners;
    }

    public static void writeOwners(Parcel dest, int flags, List<Owner> owners){
        if(owners == null){
            dest.writeInt(1);
            return;
        }

        dest.writeInt(owners.size());
        for(Owner owner : owners){
            writeOwner(dest, flags, owner);
        }
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
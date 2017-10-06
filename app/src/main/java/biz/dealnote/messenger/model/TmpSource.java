package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public final class TmpSource implements Parcelable {

    private final int ownerId;

    private final int sourceId;

    public TmpSource(int ownerId, int sourceId) {
        this.ownerId = ownerId;
        this.sourceId = sourceId;
    }

    private TmpSource(Parcel in) {
        ownerId = in.readInt();
        sourceId = in.readInt();
    }

    public static final Creator<TmpSource> CREATOR = new Creator<TmpSource>() {
        @Override
        public TmpSource createFromParcel(Parcel in) {
            return new TmpSource(in);
        }

        @Override
        public TmpSource[] newArray(int size) {
            return new TmpSource[size];
        }
    };

    public int getOwnerId() {
        return ownerId;
    }

    public int getSourceId() {
        return sourceId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ownerId);
        dest.writeInt(sourceId);
    }
}
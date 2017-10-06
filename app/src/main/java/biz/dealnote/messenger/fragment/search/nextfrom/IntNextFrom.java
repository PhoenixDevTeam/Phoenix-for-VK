package biz.dealnote.messenger.fragment.search.nextfrom;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hp-dv6 on 08.06.2016 with Core i7 2670QM.
 * VKMessenger
 */
public class IntNextFrom extends AbsNextFrom implements Parcelable {

    private int offset;

    public IntNextFrom(int initValue){
        this.offset = initValue;
    }

    protected IntNextFrom(Parcel in) {
        offset = in.readInt();
    }

    public static final Creator<IntNextFrom> CREATOR = new Creator<IntNextFrom>() {
        @Override
        public IntNextFrom createFromParcel(Parcel in) {
            return new IntNextFrom(in);
        }

        @Override
        public IntNextFrom[] newArray(int size) {
            return new IntNextFrom[size];
        }
    };

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(offset);
    }

    @Override
    public void reset() {
        offset = 0;
    }
}

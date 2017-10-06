package biz.dealnote.messenger.model.selection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 16.08.2017.
 * phoenix
 */
public class LocalPhotosSelectableSource extends AbsSelectableSource implements Parcelable {

    public LocalPhotosSelectableSource() {
        super(Types.LOCAL_PHOTOS);
    }

    protected LocalPhotosSelectableSource(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<LocalPhotosSelectableSource> CREATOR = new Creator<LocalPhotosSelectableSource>() {
        @Override
        public LocalPhotosSelectableSource createFromParcel(Parcel in) {
            return new LocalPhotosSelectableSource(in);
        }

        @Override
        public LocalPhotosSelectableSource[] newArray(int size) {
            return new LocalPhotosSelectableSource[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
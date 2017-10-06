package biz.dealnote.messenger.model.selection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 16.08.2017.
 * phoenix
 */
public class FileManagerSelectableSource extends AbsSelectableSource implements Parcelable {

    public FileManagerSelectableSource() {
        super(Types.FILES);
    }

    protected FileManagerSelectableSource(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<FileManagerSelectableSource> CREATOR = new Creator<FileManagerSelectableSource>() {
        @Override
        public FileManagerSelectableSource createFromParcel(Parcel in) {
            return new FileManagerSelectableSource(in);
        }

        @Override
        public FileManagerSelectableSource[] newArray(int size) {
            return new FileManagerSelectableSource[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
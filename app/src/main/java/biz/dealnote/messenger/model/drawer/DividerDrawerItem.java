package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class DividerDrawerItem extends AbsDrawerItem implements Parcelable {

    public DividerDrawerItem() {
        super(TYPE_DIVIDER);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public DividerDrawerItem(Parcel in) {
        super(in);
    }

    public static Creator<DividerDrawerItem> CREATOR = new Creator<DividerDrawerItem>() {
        public DividerDrawerItem createFromParcel(Parcel source) {
            return new DividerDrawerItem(source);
        }

        public DividerDrawerItem[] newArray(int size) {
            return new DividerDrawerItem[size];
        }
    };

}

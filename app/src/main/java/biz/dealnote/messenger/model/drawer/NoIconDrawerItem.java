package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class NoIconDrawerItem extends SectionDrawerItem implements Parcelable {

    public NoIconDrawerItem(int section, int title) {
        super(TYPE_WITHOUT_ICON, section, title);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public NoIconDrawerItem(Parcel in) {
        super(in);
    }

    public static Creator<NoIconDrawerItem> CREATOR = new Creator<NoIconDrawerItem>() {
        public NoIconDrawerItem createFromParcel(Parcel source) {
            return new NoIconDrawerItem(source);
        }

        public NoIconDrawerItem[] newArray(int size) {
            return new NoIconDrawerItem[size];
        }
    };
}

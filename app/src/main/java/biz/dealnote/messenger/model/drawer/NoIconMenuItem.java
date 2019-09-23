package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class NoIconMenuItem extends SectionMenuItem implements Parcelable {

    public static Creator<NoIconMenuItem> CREATOR = new Creator<NoIconMenuItem>() {
        public NoIconMenuItem createFromParcel(Parcel source) {
            return new NoIconMenuItem(source);
        }

        public NoIconMenuItem[] newArray(int size) {
            return new NoIconMenuItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public NoIconMenuItem(int section, int title) {
        super(TYPE_WITHOUT_ICON, section, title);
    }

    public NoIconMenuItem(Parcel in) {
        super(in);
    }
}

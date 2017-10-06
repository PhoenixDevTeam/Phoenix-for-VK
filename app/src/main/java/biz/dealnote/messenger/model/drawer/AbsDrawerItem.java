package biz.dealnote.messenger.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class AbsDrawerItem implements Parcelable {

    public static final int TYPE_WITH_ICON = 0;
    public static final int TYPE_WITHOUT_ICON = 1;
    public static final int TYPE_DIVIDER = 2;
    public static final int TYPE_RECENT_CHAT = 3;

    public AbsDrawerItem(int type) {
        this.type = type;
    }

    private int type;

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    public AbsDrawerItem(Parcel in) {
        this.type = in.readInt();
    }

    public static Creator<AbsDrawerItem> CREATOR = new Creator<AbsDrawerItem>() {
        public AbsDrawerItem createFromParcel(Parcel source) {
            return new AbsDrawerItem(source);
        }

        public AbsDrawerItem[] newArray(int size) {
            return new AbsDrawerItem[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsDrawerItem that = (AbsDrawerItem) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }
}